package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.definitions.Definitions;
import com.venta.engine.enums.ShaderLightUniform;
import com.venta.engine.enums.ShaderUniform;
import com.venta.engine.exceptions.ProgramLinkException;
import com.venta.engine.model.dto.ProgramDTO;
import com.venta.engine.model.view.ProgramView;
import com.venta.engine.model.view.ShaderView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramManager extends AbstractManager<ProgramManager.ProgramEntity, ProgramView> {
    private final ShaderManager.ShaderAccessor shaderAccessor;
    private final ResourceManager resourceManager;
    private final ShaderManager shaderManager;

    public ProgramView load(final String name) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading program {}", name);

        final var programDTO = resourceManager.load(String.format("/programs/%s.json", name), ProgramDTO.class);

        return store(create(programDTO.name(),
                shaderManager.load(programDTO.shaderVertex(), ShaderManager.ShaderEntity.Type.Vertex),
                shaderManager.load(programDTO.shaderFragment(), ShaderManager.ShaderEntity.Type.Fragment)));
    }

    private void registerUniforms(final ProgramEntity program) {
        for (final var field : ShaderUniform.values())
            program.addUniformID(field.getUniformName(), glGetUniformLocation(program.getInternalID(), field.getUniformName()));

        for (int i = 0; i < Definitions.LIGHT_MAX; i++)
            for (final var field : ShaderLightUniform.values())
                program.addUniformID(field.getUniformName(i), glGetUniformLocation(program.getInternalID(), field.getUniformName(i)));

        log.debug("{} uniforms found and registered for program {}", program.uniforms.size(), program.getID());
    }

    private ProgramEntity create(final String name, @NonNull final ShaderView shaderVertex, @NonNull final ShaderView shaderFragment) {
        log.info("Creating program {}", name);
        final var id = glCreateProgram();

        glAttachShader(id, shaderAccessor.get(shaderVertex).getInternalID());
        glAttachShader(id, shaderAccessor.get(shaderFragment).getInternalID());

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

    @Getter
    public static final class ProgramEntity extends AbstractManager.AbstractEntity implements ProgramView {
        private final int internalID;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> uniforms = new HashMap<>();

        ProgramEntity(final int internalID, @NonNull final String name) {
            super(name);

            this.internalID = internalID;
        }

        private void addUniformID(final String name, final Integer uniformID) {
            if (uniformID >= 0)
                this.uniforms.put(name, uniformID);
        }

        public int getUniformID(final String name) {
            return uniforms.getOrDefault(name, -1);
        }

        public int getUniformID(final ShaderUniform uniform) {
            return getUniformID(uniform.getUniformName());
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ProgramAccessor extends AbstractAccessor {}
}
