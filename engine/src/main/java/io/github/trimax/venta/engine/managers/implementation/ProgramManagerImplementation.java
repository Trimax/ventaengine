package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ShaderLightUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.exceptions.ProgramLinkException;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.model.dto.ProgramDTO;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.ShaderEntity;
import io.github.trimax.venta.engine.model.view.ProgramView;
import io.github.trimax.venta.engine.model.view.ShaderView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramManagerImplementation
        extends AbstractManagerImplementation<ProgramEntity, ProgramView>
        implements ProgramManager {
    private final ResourceManagerImplementation resourceManager;
    private final ShaderManagerImplementation shaderManager;

    @Override
    public ProgramEntity load(@NonNull final String name) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading program {}", name);

        final var programDTO = resourceManager.load(String.format("/programs/%s.json", name), ProgramDTO.class);

        return store(create(name,
                shaderManager.load(programDTO.shaderVertex(), ShaderEntity.Type.Vertex),
                shaderManager.load(programDTO.shaderFragment(), ShaderEntity.Type.Fragment)));
    }

    private void registerUniforms(final ProgramEntity program) {
        for (final var field : ShaderUniform.values())
            program.addUniformID(field.getUniformName(), glGetUniformLocation(program.getInternalID(), field.getUniformName()));

        for (int i = 0; i < Definitions.LIGHT_MAX; i++)
            for (final var field : ShaderLightUniform.values())
                program.addUniformID(field.getUniformName(i), glGetUniformLocation(program.getInternalID(), field.getUniformName(i)));

        log.debug("{} uniforms found and registered for program {}", program.getUniformCount(), program.getID());
    }

    private ProgramEntity create(@NonNull final String name,
                                 @NonNull final ShaderView shaderVertex,
                                 @NonNull final ShaderView shaderFragment) {
        log.info("Creating program {}", name);
        final var id = glCreateProgram();

        glAttachShader(id, shaderManager.getEntity(shaderVertex.getID()).getInternalID());
        glAttachShader(id, shaderManager.getEntity(shaderFragment.getID()).getInternalID());

        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            glDeleteProgram(id);

            throw new ProgramLinkException(message);
        }

        glUseProgram(id);

        final var program = new ProgramEntity(id, name);
        registerUniforms(program);

        return program;
    }

    @Override
    protected void destroy(final ProgramEntity program) {
        log.info("Destroying program {} ({})", program.getID(), program.getName());
        glDeleteProgram(program.getInternalID());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
