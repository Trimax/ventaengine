package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.prefabs.BillboardPrefab;
import lombok.NonNull;

public interface BillboardManager extends AbstractManager<BillboardInstance> {
    BillboardInstance create(@NonNull final String name,
                             @NonNull final BillboardPrefab prefab);

    void delete(@NonNull final BillboardInstance instance);
}
