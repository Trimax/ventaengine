package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.parameters.LightParameters;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.NonNull;

public interface LightManager extends AbstractManager<LightInstance> {
    LightInstance create(@NonNull final String name,
                         @NonNull final LightPrefab prefab);

    LightInstance create(@NonNull final String name,
                         @NonNull final LightParameters parameters);
}
