package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import com.venta.engine.exception.ProgramLinkException;
import com.venta.engine.model.VentaProgram;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ProgramManager extends AbstractManager<ProgramManager.ProgramEntity> {
    private final ResourceManager resourceManager;
    private final ShaderManager shaderManager;

    public ProgramEntity load(final String name) {
        log.info("Loading shader {}", name);

        final var parsedProgram = resourceManager.load(String.format("/programs/%s.json", name), VentaProgram.class);
        final var id = glCreateProgram();

        final var shaders = StreamEx.of(parsedProgram.shaders()).map(shaderManager::load).toList();
        StreamEx.of(shaders).map(ShaderManager.ShaderEntity::getIdAsInteger).forEach(shaderID -> glAttachShader(id, shaderID));
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            glDeleteProgram(id);
            throw new ProgramLinkException(message);
        }

        final var program = new ProgramEntity(id, parsedProgram.name(), shaders);
        for (final String uniform : parsedProgram.uniforms())
            program.uniforms.put(uniform, glGetUniformLocation(id, uniform));

        return store(program);
    }

    public ProgramEntity link(final String name, final ShaderManager.ShaderEntity... shaders) {
        if (ArrayUtils.isEmpty(shaders))
            throw new ProgramLinkException(name);

        log.info("Creating program {}", name);
        final var id = glCreateProgram();

        StreamEx.of(shaders).map(ShaderManager.ShaderEntity::getIdAsInteger).forEach(shaderID -> glAttachShader(id, shaderID));
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            glDeleteProgram(id);
            throw new ProgramLinkException(message);
        }

        return store(new ProgramEntity(id, name, List.of(shaders)));
    }

    @Override
    protected void destroy(final ProgramEntity program) {
        log.info("Unlinking program {}", program.getName());
        glDeleteProgram(program.getIdAsInteger());
    }

    @Getter
    public static final class ProgramEntity extends AbstractManager.AbstractEntity {
        private final String name;

        @Getter(AccessLevel.NONE)
        private final List<ShaderManager.ShaderEntity> shaders;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> uniforms = new HashMap<>();

        ProgramEntity(final long id, @NonNull final String name, @NonNull final List<ShaderManager.ShaderEntity> shaders) {
            super(id);

            this.name = name;
            this.shaders = shaders;
        }

        public int getUniformID(final String name) {
            return uniforms.get(name);
        }
    }
}
