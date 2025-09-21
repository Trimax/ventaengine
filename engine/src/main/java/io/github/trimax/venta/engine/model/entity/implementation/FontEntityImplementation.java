package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import io.github.trimax.venta.engine.model.entity.FontEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class FontEntityImplementation extends AbstractEntityImplementation implements FontEntity {
    List<AtlasEntityImplementation> atlases = new ArrayList<>();

    @NonNull
    ByteBuffer buffer;

    public void add(final AtlasEntity atlas) {
        if (atlas instanceof AtlasEntityImplementation a)
            this.atlases.add(a);
    }
}