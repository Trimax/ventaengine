package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.CameraView;
import lombok.NonNull;

public interface CameraManager extends AbstractManager<CameraView> {
    CameraView create(@NonNull final String name);

    CameraView getCurrent();

    void setCurrent(@NonNull final CameraView camera);
}
