package io.github.trimax.venta.engine.registries.implementation;

import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL20C.*;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.ShaderLightUniform;
import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.ShaderWaveUniform;
import io.github.trimax.venta.engine.exceptions.ProgramLinkException;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.dto.ProgramDTO;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.registries.ProgramRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramRegistryImplementation
        extends AbstractRegistryImplementation<ProgramEntityImplementation, ProgramEntity, Void>
        implements ProgramRegistry {
    private final ShaderRegistryImplementation shaderRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    protected ProgramEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading program {}", resourcePath);

        final var programDTO = resourceService.getAsObject(String.format("/programs/%s", resourcePath), ProgramDTO.class);

        final var id = memory.getPrograms().create(resourcePath);
        glAttachShader(id, shaderRegistry.get(programDTO.shaderVertex(), ShaderType.Vertex).getInternalID());
        glAttachShader(id, shaderRegistry.get(programDTO.shaderFragment(), ShaderType.Fragment).getInternalID());

        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            memory.getPrograms().delete(id);

            throw new ProgramLinkException(message);
        }

        glUseProgram(id);

        final var program = abettor.createProgram(id);
        registerUniforms(program);

        return program;
    }

    private void registerUniforms(final ProgramEntityImplementation program) {
        for (final var field : ShaderUniform.values())
            program.addUniformID(field.getUniformName(), glGetUniformLocation(program.getInternalID(), field.getUniformName()));

        for (int i = 0; i < DefinitionsCommon.LIGHT_MAX; i++)
            for (final var field : ShaderLightUniform.values())
                program.addUniformID(field.getUniformName(i), glGetUniformLocation(program.getInternalID(), field.getUniformName(i)));

        for (int i = 0; i < DefinitionsCommon.WAVE_MAX; i++)
            for (final var field : ShaderWaveUniform.values())
                program.addUniformID(field.getUniformName(i), glGetUniformLocation(program.getInternalID(), field.getUniformName(i)));

        log.debug("{} uniforms found and registered for program {}", program.getUniformCount(), program.getID());
    }

    @Override
    protected void unload(@NonNull final ProgramEntityImplementation entity) {
        log.info("Unloading program {}", entity.getID());

        memory.getPrograms().delete(entity.getInternalID());
    }
}
