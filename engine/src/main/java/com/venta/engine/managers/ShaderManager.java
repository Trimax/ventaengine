package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ShaderCompileException;
import com.venta.engine.model.view.ShaderView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL20C;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderManager extends AbstractManager<ShaderManager.ShaderEntity, ShaderView> {
    private final ResourceManager resourceManager;

    public ShaderView load(final String name, final ShaderManager.ShaderEntity.Type type) {
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
        log.info("Destroying shader {} ({})", shader.getID(), shader.getName());
        glDeleteShader(shader.getInternalID());
    }

    @Getter
    public static final class ShaderEntity extends AbstractEntity implements com.venta.engine.model.view.ShaderView {
        private final int internalID;
        private final Type type;
        private final String name;

        @Getter(AccessLevel.NONE)
        private final String code;

        ShaderEntity(final int internalID, @NonNull final Type type, @NonNull final String name, @NonNull final String code) {
            this.internalID = internalID;
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

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ShaderAccessor extends AbstractAccessor {}
}
