package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.TerrainSurfaceManager;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.TerrainSurfacePrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TerrainSurfaceManagerImplementation
        extends AbstractManagerImplementation<TerrainSurfaceInstanceImplementation, TerrainSurfaceInstance>
        implements TerrainSurfaceManager {
    private final Abettor abettor;

    @Override
    public TerrainSurfaceInstance create(@NonNull final String name, @NonNull final TerrainSurfacePrefab prefab) {
        if (prefab instanceof TerrainSurfacePrefabImplementation terrainSurface)
            return create(name, terrainSurface);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private TerrainSurfaceInstance create(final String name, final TerrainSurfacePrefabImplementation prefab) {
        return store(abettor.createTerrainSurface(name,
                prefab.getGridMesh(),
                prefab.getProgram(),
                prefab.getMaterial()));
    }

    @Override
    public void delete(@NonNull final TerrainSurfaceInstance instance) {
        if (instance instanceof TerrainSurfaceInstanceImplementation object)
            super.delete(object);
    }

    @Override
    protected void destroy(final TerrainSurfaceInstanceImplementation object) {
        log.info("Destroying terrain surface {} ({})", object.getID(), object.getName());
    }
}
