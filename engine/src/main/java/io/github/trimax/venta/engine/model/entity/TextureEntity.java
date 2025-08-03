package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.memory.MemoryBlock;
import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class TextureEntity extends AbstractEntity implements TextureView {
    private final MemoryBlock<Integer> internal;
    private final int width;
    private final int height;

    public TextureEntity(@NonNull final String name,
                         @NonNull final MemoryBlock<Integer> internal,
                         final int width,
                         final int height) {
        super(name);

        this.internal = internal;
        this.width = width;
        this.height = height;
    }
}
