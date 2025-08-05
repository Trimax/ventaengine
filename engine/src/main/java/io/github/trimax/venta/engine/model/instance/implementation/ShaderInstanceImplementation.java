package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.ShaderInstance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.lwjgl.opengl.GL20C;

@Getter
public final class ShaderInstanceImplementation extends AbstractInstanceImplementation implements ShaderInstance {
    private final int internalID;
    private final String code;
    private final Type type;

    public ShaderInstanceImplementation(final int internalID, @NonNull final Type type, @NonNull final String name, @NonNull final String code) {
        super(name);

        this.internalID = internalID;
        this.type = type;
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
