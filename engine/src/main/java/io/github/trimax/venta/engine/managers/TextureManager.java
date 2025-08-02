package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.NonNull;

public interface TextureManager extends AbstractManager<TextureView> {
    TextureView load(@NonNull final String name);
}
