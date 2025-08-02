package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.LightView;
import lombok.NonNull;

public interface LightManager extends AbstractManager<LightView> {
    LightView load(@NonNull final String name);
}
