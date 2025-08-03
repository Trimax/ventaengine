package io.github.trimax.venta.engine.executors.camera;

import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractCameraExecutor extends AbstractExecutor {
    protected AbstractCameraExecutor(@NonNull final InternalVentaContext context,
                                     @NonNull final String command,
                                     @NonNull final String description) {
        super(context, command, description, null);
    }
}
