package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.view.FontView;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class FontInstance extends AbstractInstance implements FontView {
    private final List<AtlasInstance> atlases = new ArrayList<>();
    private final ByteBuffer buffer;

    public FontInstance(@NonNull final String name,
                        @NonNull final ByteBuffer buffer) {
        super(name);

        this.buffer = buffer;
    }

    public void add(final AtlasInstance atlas) {
        this.atlases.add(atlas);
    }
}
