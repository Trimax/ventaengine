package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.FontInstance;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class FontInstanceImplementation extends AbstractInstanceImplementation implements FontInstance {
    private final List<AtlasInstanceImplementation> atlases = new ArrayList<>();
    private final ByteBuffer buffer;

    public FontInstanceImplementation(@NonNull final String name,
                                      @NonNull final ByteBuffer buffer) {
        super(name);

        this.buffer = buffer;
    }

    public void add(final AtlasInstanceImplementation atlas) {
        this.atlases.add(atlas);
    }
}
