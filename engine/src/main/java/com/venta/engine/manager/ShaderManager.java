package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import com.venta.engine.exception.ShaderCompileException;
import com.venta.engine.exception.UnknownShaderTypeException;
import com.venta.engine.model.parsing.VentaShader;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL20C;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ShaderManager extends AbstractManager<ShaderManager.ShaderEntity> {
    private final ResourceManager resourceManager;

    public ShaderEntity load(final String name) {
        log.info("Loading shader {}", name);

        return load(name, resourceManager.load(String.format("/shaders/%s.json", name), VentaShader.class));
    }

    private ShaderEntity load(final String name, final VentaShader parsedShader) {
        final var shaderType = ShaderEntity.Type.parse(parsedShader.type());

        final var code = resourceManager.load(String.format("/shaders/%s", parsedShader.path()));
        final var id = glCreateShader(shaderType.getValue());

        glShaderSource(id, code);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            throw new ShaderCompileException(glGetShaderInfoLog(id));

        final var shader = new ShaderEntity(id, shaderType, name, code);
        if (parsedShader.attributes() != null)
            shader.attributes.putAll(parsedShader.attributes());

        return store(shader);
    }

    @Override
    protected void destroy(final ShaderEntity shader) {
        log.info("Deleting shader {}", shader.getName());
        glDeleteShader(shader.getIdAsInteger());
    }

    @Getter
    public static final class ShaderEntity extends AbstractEntity {
        private final Type type;
        private final String name;

        @Getter(AccessLevel.NONE)
        private final String code;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> attributes = new HashMap<>();

        ShaderEntity(final long id, @NonNull final Type type, @NonNull final String name, @NonNull final String code) {
            super(id);

            this.type = type;
            this.name = name;
            this.code = code;
        }

        public int getAttribute(final String name) {
            return attributes.get(name);
        }

        @Getter
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public enum Type {
            Vertex(GL20C.GL_VERTEX_SHADER),
            Fragment(GL20C.GL_FRAGMENT_SHADER);

            private final int value;

            public static Type parse(final String value) {
                for (final Type type : Type.values())
                    if (type.name().equalsIgnoreCase(value))
                        return type;

                throw new UnknownShaderTypeException(value);
            }
        }
    }
}
