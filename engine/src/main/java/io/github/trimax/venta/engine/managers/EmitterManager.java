package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import lombok.NonNull;

public interface EmitterManager extends AbstractManager<EmitterInstance> {
    EmitterInstance create(@NonNull final String name,
                           @NonNull final EmitterPrefab prefab);

    void delete(@NonNull final EmitterInstance instance);
}
