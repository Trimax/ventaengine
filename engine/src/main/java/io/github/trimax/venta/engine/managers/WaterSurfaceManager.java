package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.WaterSurfaceInstance;
import io.github.trimax.venta.engine.model.prefabs.WaterSurfacePrefab;
import lombok.NonNull;

public interface WaterSurfaceManager extends AbstractManager<WaterSurfaceInstance> {
    WaterSurfaceInstance create(@NonNull final String name,
                                @NonNull final WaterSurfacePrefab prefab);

    void delete(@NonNull final WaterSurfaceInstance instance);
}
