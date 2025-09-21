package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.GridMeshManager;
import io.github.trimax.venta.engine.model.instance.GridMeshInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.GridMeshInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.GridMeshPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshManagerImplementation
        extends AbstractManagerImplementation<GridMeshInstanceImplementation, GridMeshInstance>
        implements GridMeshManager {
    private final Abettor abettor;

    @Override
    public GridMeshInstance create(@NonNull final String name, @NonNull final GridMeshPrefab prefab) {
        if (prefab instanceof GridMeshPrefabImplementation gridMesh)
            return create(name, gridMesh);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private GridMeshInstance create(final String name, final GridMeshPrefabImplementation prefab) {
        return store(abettor.createGridMesh(name, prefab.getProgram(), prefab.getMaterial(), prefab.getGeometry(), prefab.getWaves()));
    }

    @Override
    public void delete(@NonNull final GridMeshInstance instance) {
        if (instance instanceof GridMeshInstanceImplementation object)
            super.delete(object);
    }

    @Override
    protected void destroy(final GridMeshInstanceImplementation object) {
        log.info("Destroying gridmesh {} ({})", object.getID(), object.getName());
    }
}
