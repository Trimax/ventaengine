package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.WaterSurfaceManager;
import io.github.trimax.venta.engine.model.instance.WaterSurfaceInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.WaterSurfacePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.WaterSurfacePrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterSurfaceManagerImplementation
        extends AbstractManagerImplementation<WaterSurfaceInstanceImplementation, WaterSurfaceInstance>
        implements WaterSurfaceManager {
    private final Abettor abettor;

    @Override
    public WaterSurfaceInstance create(@NonNull final String name, @NonNull final WaterSurfacePrefab prefab) {
        if (prefab instanceof WaterSurfacePrefabImplementation waterSurface)
            return create(name, waterSurface);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private WaterSurfaceInstance create(final String name, final WaterSurfacePrefabImplementation prefab) {
        return store(abettor.createWaterSurface(name,
                prefab.getGridMesh(),
                prefab.getProgram(),
                prefab.getM(),
                prefab.getFoam(),
                prefab.getNoises(),
                prefab.getWaves()));
    }

    @Override
    public void delete(@NonNull final WaterSurfaceInstance instance) {
        if (instance instanceof WaterSurfaceInstanceImplementation object)
            super.delete(object);
    }

    @Override
    protected void destroy(final WaterSurfaceInstanceImplementation object) {
        log.info("Destroying grid mesh {} ({})", object.getID(), object.getName());
    }
}
