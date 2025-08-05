package io.github.trimax.venta.engine.executors.camera;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractCameraExecutor extends AbstractExecutor {
    protected AbstractCameraExecutor(@NonNull final ControllerFactory factory,
                                     @NonNull final String command,
                                     @NonNull final String description) {
        super(factory, command, description);
    }
}
