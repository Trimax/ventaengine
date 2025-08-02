package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.MeshView;
import lombok.NonNull;

public interface MeshManager extends AbstractManager<MeshView> {
    MeshView load(@NonNull final String name);
}
