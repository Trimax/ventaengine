package com.venta.engine.managers;

import static org.lwjgl.opengl.GL20C.*;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL20C;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ShaderCompileException;
import com.venta.engine.exceptions.UnknownShaderTypeException;
import com.venta.engine.model.dto.ShaderDTO;
import com.venta.engine.model.views.ShaderView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ShaderManager extends AbstractManager<ShaderManager.ShaderEntity, ShaderView> {
    private final ResourceManager resourceManager;

    public ShaderView load(final String name) {
        log.info("Loading shader {}", name);

        return load(name, resourceManager.load(String.format("/shaders/%s.json", name), ShaderDTO.class));
    }

    private ShaderView load(final String name, final ShaderDTO parsedShader) {
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
        glDeleteShader(shader.getInternalID());
    }

    @Getter
    public static final class ShaderEntity extends AbstractEntity implements com.venta.engine.model.views.ShaderView {
        private final int internalID;
        private final Type type;
        private final String name;

        @Getter(AccessLevel.NONE)
        private final String code;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> attributes = new HashMap<>();

        ShaderEntity(final int internalID, @NonNull final Type type, @NonNull final String name, @NonNull final String code) {
            this.internalID = internalID;
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

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ShaderAccessor extends AbstractAccessor {}
}
