package io.github.trimax.venta.engine.model.entities;

import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class TextureEntity extends AbstractEntity implements TextureView {
    private final int internalID;
    private final int width;
    private final int height;

    public TextureEntity(final int internalID,
                         @NonNull final String name,
                         final int width,
                         final int height) {
        super(name);

        this.internalID = internalID;
        this.width = width;
        this.height = height;
    }
}
