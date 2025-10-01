package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import lombok.NonNull;

public interface TerrainSurfaceManager extends AbstractManager<TerrainSurfaceInstance> {
    TerrainSurfaceInstance create(@NonNull final String name,
                                  @NonNull final TerrainSurfacePrefab prefab);

    void delete(@NonNull final TerrainSurfaceInstance instance);
}
