package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.TextureInstance;
import lombok.NonNull;

public interface TextureManager extends AbstractManager<TextureInstance> {
    TextureInstance load(@NonNull final String name);
}
