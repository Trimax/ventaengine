package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.NonNull;

public interface CameraManager extends AbstractManager<CameraInstance> {
    CameraInstance create(@NonNull final String name);

    CameraInstance getCurrent();

    void setCurrent(@NonNull final CameraInstance camera);
}
