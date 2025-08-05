package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import io.github.trimax.venta.engine.model.entity.FontEntity;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class FontEntityImplementation extends AbstractEntityImplementation implements FontEntity {
    private final List<AtlasEntityImplementation> atlases = new ArrayList<>();
    private final ByteBuffer buffer;

    public FontEntityImplementation(@NonNull final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void add(final AtlasEntity atlas) {
        if (atlas instanceof AtlasEntityImplementation a)
            this.atlases.add(a);
    }
}