package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import lombok.NonNull;

public interface ObjectManager extends AbstractManager<ObjectInstance> {
    ObjectInstance create(@NonNull final String name,
                          @NonNull final ObjectPrefab prefab);
}
