package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;

@Getter
public final class TextureEntity extends AbstractEntity implements TextureView {
    private final ByteBuffer buffer;
    private final int internalID;
    private final int width;
    private final int height;

    public TextureEntity(@NonNull final String name,
                         @NonNull final ByteBuffer buffer,
                         final int internalID,
                         final int width,
                         final int height) {
        super(name);

        this.internalID = internalID;
        this.buffer = buffer;
        this.height = height;
        this.width = width;
    }
}
