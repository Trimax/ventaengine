package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.MeshInstance;
import lombok.NonNull;

public interface MeshManager extends AbstractManager<MeshInstance> {
    MeshInstance load(@NonNull final String name);
}
