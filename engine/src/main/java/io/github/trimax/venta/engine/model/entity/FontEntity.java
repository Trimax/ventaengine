package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.FontView;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class FontEntity extends AbstractEntity implements FontView {
    private final List<AtlasEntity> atlases = new ArrayList<>();
    private final ByteBuffer buffer;

    public FontEntity(@NonNull final String name,
                      @NonNull final ByteBuffer buffer) {
        super(name);

        this.buffer = buffer;
    }

    public void add(final AtlasEntity atlas) {
        this.atlases.add(atlas);
    }
}
