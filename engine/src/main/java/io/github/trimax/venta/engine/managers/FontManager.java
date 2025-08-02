package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.FontView;
import lombok.NonNull;

public interface FontManager extends AbstractManager<FontView> {
    FontView load(@NonNull final String name);
}
