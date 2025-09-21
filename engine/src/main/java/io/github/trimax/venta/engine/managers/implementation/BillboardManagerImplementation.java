package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.BillboardManager;
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.BillboardInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.BillboardPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.BillboardPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BillboardManagerImplementation
        extends AbstractManagerImplementation<BillboardInstanceImplementation, BillboardInstance>
        implements BillboardManager {
    private final Abettor abettor;

    @Override
    public BillboardInstance create(@NonNull final String name, @NonNull final BillboardPrefab prefab) {
        if (prefab instanceof BillboardPrefabImplementation billboard)
            return create(name, billboard);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private BillboardInstance create(final String name, final BillboardPrefabImplementation prefab) {
        return store(abettor.createBillboard(name,
                prefab.getProgram(),
                prefab.getSprite(),
                prefab.getGeometry(),
                prefab.getSize()));
    }

    @Override
    public void delete(@NonNull final BillboardInstance instance) {
        if (instance instanceof BillboardInstanceImplementation billboard)
            super.delete(billboard);
    }

    @Override
    protected void destroy(final BillboardInstanceImplementation billboard) {
        log.info("Destroying billboard {} ({})", billboard.getID(), billboard.getName());
    }
}
