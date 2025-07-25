package com.venta.engine.managers;

import static org.lwjgl.opengl.GL20C.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.venta.engine.annotations.Component;
import com.venta.engine.definitions.Definitions;
import com.venta.engine.enums.ShaderUniform;
import com.venta.engine.exceptions.ProgramLinkException;
import com.venta.engine.model.dto.ProgramDTO;
import com.venta.engine.model.view.AbstractView;
import com.venta.engine.model.view.ProgramView;
import com.venta.engine.model.view.ShaderView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramManager extends AbstractManager<ProgramManager.ProgramEntity, ProgramView> {
    private final ResourceManager resourceManager;
    private final ShaderManager shaderManager;

    public ProgramView load(final String name) {
        log.info("Loading program {}", name);

        final var programDTO = resourceManager.load(String.format("/programs/%s.json", name), ProgramDTO.class);

        final ProgramEntity program = create(programDTO.name(), StreamEx.of(programDTO.shaders()).map(shaderManager::load).toList());

        glUseProgram(program.getInternalID());
        for (final String uniform : programDTO.uniforms()) {
            final var uniformLocationID = glGetUniformLocation(program.getInternalID(), uniform);
            program.uniforms.put(uniform, uniformLocationID);
            if (uniformLocationID == -1)
                log.warn("Uniform '{}' not found in program {}", uniform, program.getName());
        }

        registerLightUniforms(program);

        return store(program);
    }

    //TODO: Rewrite it in more clean style
    private static void registerLightUniforms(final ProgramEntity program) {
        final String[] lightFields = {
                "type",
                "position",
                "direction",
                "color",
                "intensity",
                "attenuation.constant",
                "attenuation.linear",
                "attenuation.quadratic",
                "enabled",
                "castShadows"
        };

        for (int i = 0; i < Definitions.LIGHT_MAX; i++) {
            for (final var field : lightFields) {
                final var uniformName = String.format("lights[%d].%s", i, field);
                program.uniforms.put(uniformName, glGetUniformLocation(program.getInternalID(), uniformName));
            }
        }

        final var lightCountLoc = glGetUniformLocation(program.getInternalID(), "lightCount");
        if (lightCountLoc >= 0)
            program.uniforms.put("lightCount", lightCountLoc);
    }

    public ProgramView create(final String name, final ShaderView... shaders) {
        if (ArrayUtils.isEmpty(shaders))
            throw new ProgramLinkException(name);

        return store(create(name, List.of(shaders)));
    }

    private ProgramEntity create(final String name, final List<ShaderView> shaders) {
        if (CollectionUtils.isEmpty(shaders))
            throw new ProgramLinkException(name);

        log.info("Creating program {}", name);
        final var id = glCreateProgram();

        StreamEx.of(shaders)
                .map(AbstractView::getID)
                .map(shaderManager::get)
                .map(ShaderManager.ShaderEntity::getInternalID)
                .forEach(shaderID -> glAttachShader(id, shaderID));
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            glDeleteProgram(id);

            throw new ProgramLinkException(message);
        }

        return new ProgramEntity(id, name);
    }

    @Override
    protected void destroy(final ProgramEntity program) {
        log.info("Deleting program {}", program.getName());
        glDeleteProgram(program.getInternalID());
    }

    @Getter
    public static final class ProgramEntity extends AbstractManager.AbstractEntity implements
            com.venta.engine.model.view.ProgramView {
        private final int internalID;
        private final String name;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> uniforms = new HashMap<>();

        ProgramEntity(final int internalID, @NonNull final String name) {
            this.internalID = internalID;
            this.name = name;
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
