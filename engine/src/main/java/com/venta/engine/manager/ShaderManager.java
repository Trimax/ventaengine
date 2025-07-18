package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import com.venta.engine.exception.ShaderCompileException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL20C;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ShaderManager extends AbstractManager<ShaderManager.ShaderEntity> {
    private final ResourceManager resourceManager;

    public ShaderEntity loadVertexShader(final String name) {
        return load(String.format("vertex/%s", name), ShaderEntity.Type.Vertex);
    }

    public ShaderEntity loadFragmentShader(final String name) {
        return load(String.format("fragment/%s", name), ShaderEntity.Type.Fragment);
    }

    private ShaderEntity load(final String name, final ShaderEntity.Type type) {
        log.info("Loading shader {}", name);

        final var code = resourceManager.load(String.format("/shaders/%s", name));
        final var id = glCreateShader(type.getValue());

        glShaderSource(id, code);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            throw new ShaderCompileException(glGetShaderInfoLog(id));

        return store(new ShaderEntity(id, type, name, code));
    }

    @Override
    protected void destroy(final ShaderEntity shader) {
        log.info("Unloading shader {}", shader.getName());
        glDeleteShader(shader.getIdAsInteger());
    }

    @Getter
    public static final class ShaderEntity extends AbstractEntity {
        private final Type type;
        private final String name;

        @Getter(AccessLevel.NONE)
        private final String code;

        ShaderEntity(final long id, @NonNull final Type type, @NonNull final String name, @NonNull final String code) {
            super(id);

            this.type = type;
            this.name = name;
            this.code = code;
        }

        @Getter
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public enum Type {
            Vertex(GL20C.GL_VERTEX_SHADER),
            Fragment(GL20C.GL_FRAGMENT_SHADER);

            private final int value;
        }
    }
}
