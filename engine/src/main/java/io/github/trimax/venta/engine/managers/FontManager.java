package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.FontInstance;
import lombok.NonNull;

public interface FontManager extends AbstractManager<FontInstance> {
    FontInstance load(@NonNull final String name);
}
