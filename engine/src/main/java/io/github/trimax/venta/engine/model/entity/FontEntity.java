package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.FontView;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class FontEntity extends AbstractEntity implements FontView {
    private final List<AtlasEntity> atlases = new ArrayList<>();

    public FontEntity(@NonNull final String name) {
        super(name);
    }

    public void add(final AtlasEntity atlas) {
        this.atlases.add(atlas);
    }
}
