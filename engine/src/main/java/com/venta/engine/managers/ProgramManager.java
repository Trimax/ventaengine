package com.venta.engine.managers;

import static org.lwjgl.opengl.GL20C.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ProgramLinkException;
import com.venta.engine.exceptions.ShaderArgumentException;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.dto.ProgramDTO;
import com.venta.engine.model.view.ProgramView;
import com.venta.engine.model.view.ShaderView;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ProgramManager extends AbstractManager<ProgramManager.ProgramEntity, ProgramView> {
    private final ResourceManager resourceManager;
    private final ShaderManager shaderManager;

    public ProgramView load(final String name) {
        log.info("Loading program {}", name);

        final var programDTO = resourceManager.load(String.format("/programs/%s.json", name), ProgramDTO.class);

        final ProgramEntity program = create(programDTO.name(), StreamEx.of(programDTO.shaders()).map(shaderManager::load).toList());
        for (final String uniform : programDTO.uniforms())
            program.uniforms.put(uniform, glGetUniformLocation(program.getIdAsInteger(), uniform));

        registerLightUniforms(program, 64);

        return store(program);
    }

    //TODO: Rewrite it in more clean style
    private static void registerLightUniforms(final ProgramEntity program, final int maxLights) {
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

        for (int i = 0; i < maxLights; i++) {
            for (final var field : lightFields) {
                final var uniformName = String.format("lights[%d].%s", i, field);

                final int location = glGetUniformLocation(program.getIdAsInteger(), uniformName);
                if (location >= 0)
                    program.uniforms.put(uniformName, location);
                else
                    log.warn("Uniform {} not found in program {}", uniformName, program.getIdAsInteger());
            }
        }

        final var lightCountLoc = glGetUniformLocation(program.getIdAsInteger(), "lightCount");
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
                .map(AbstractRenderer.AbstractView::getId)
                .map(shaderManager::get)
                .map(Couple::entity)
                .map(AbstractEntity::getIdAsInteger)
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
    protected ProgramView createView(final String id, final ProgramEntity entity) {
        return new ProgramView(id, entity);
    }

    @Override
    protected void destroy(final Couple<ProgramEntity, ProgramView> program) {
        log.info("Deleting program {}", program.entity().getName());
        glDeleteProgram(program.entity().getIdAsInteger());
    }

    @Getter
    public static final class ProgramEntity extends AbstractManager.AbstractEntity {
        private final String name;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> uniforms = new HashMap<>();

        ProgramEntity(final long id, @NonNull final String name) {
            super(id);

            this.name = name;
        }

        public int getUniformID(final String name) {
            final var uniformID = uniforms.get(name);
            if (uniformID == null)
                throw new ShaderArgumentException(String.format("%s (shader: %s)", name, this.name));

            return uniformID;
        }
    }
}
