package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.NonNull;

public interface LightManager extends AbstractManager<LightInstance> {
    LightInstance load(@NonNull final String name);
}
