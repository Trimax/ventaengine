package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.exception.ProgramLinkException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ProgramManager extends AbstractManager<ProgramManager.ProgramEntity> {
    static final ProgramManager instance = new ProgramManager();

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

        ProgramEntity(final long id, @NonNull final String name,  @NonNull final List<ShaderManager.ShaderEntity> shaders) {
            super(id);

            this.name = name;
            this.shaders = shaders;
        }
    }
}
