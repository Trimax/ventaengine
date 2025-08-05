package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ShaderLightUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.exceptions.ProgramLinkException;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.dto.ProgramDTO;
import io.github.trimax.venta.engine.model.instance.ProgramInstance;
import io.github.trimax.venta.engine.model.instance.ShaderInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ProgramInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ShaderInstanceImplementation;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramManagerImplementation
        extends AbstractManagerImplementation<ProgramInstanceImplementation, ProgramInstance>
        implements ProgramManager {
    private final ShaderManagerImplementation shaderManager;
    private final Memory memory;

    @Override
    public ProgramInstanceImplementation load(@NonNull final String name) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading program {}", name);

        final var programDTO = ResourceUtil.loadAsObject(String.format("/programs/%s.json", name), ProgramDTO.class);

        return store(create(name,
                shaderManager.load(programDTO.shaderVertex(), ShaderInstanceImplementation.Type.Vertex),
                shaderManager.load(programDTO.shaderFragment(), ShaderInstanceImplementation.Type.Fragment)));
    }

    private void registerUniforms(final ProgramInstanceImplementation program) {
        for (final var field : ShaderUniform.values())
            program.addUniformID(field.getUniformName(), glGetUniformLocation(program.getInternalID(), field.getUniformName()));

        for (int i = 0; i < Definitions.LIGHT_MAX; i++)
            for (final var field : ShaderLightUniform.values())
                program.addUniformID(field.getUniformName(i), glGetUniformLocation(program.getInternalID(), field.getUniformName(i)));

        log.debug("{} uniforms found and registered for program {}", program.getUniformCount(), program.getID());
    }

    private ProgramInstanceImplementation create(@NonNull final String name,
                                                 @NonNull final ShaderInstance shaderVertex,
                                                 @NonNull final ShaderInstance shaderFragment) {
        log.info("Creating program {}", name);
        final var id = memory.getPrograms().create(name);

        glAttachShader(id, shaderManager.getInstance(shaderVertex.getID()).getInternalID());
        glAttachShader(id, shaderManager.getInstance(shaderFragment.getID()).getInternalID());

        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            glDeleteProgram(id);

            throw new ProgramLinkException(message);
        }

        glUseProgram(id);

        final var program = new ProgramInstanceImplementation(id, name);
        registerUniforms(program);

        return program;
    }

    @Override
    protected void destroy(final ProgramInstanceImplementation program) {
        log.info("Destroying program {} ({})", program.getID(), program.getName());
        memory.getPrograms().delete(program.getInternalID());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
