package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.GridMeshInstance;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import lombok.NonNull;

public interface GridMeshManager extends AbstractManager<GridMeshInstance> {
    GridMeshInstance create(@NonNull final String name,
                            @NonNull final GridMeshPrefab prefab);

    void delete(@NonNull final GridMeshInstance instance);
}
