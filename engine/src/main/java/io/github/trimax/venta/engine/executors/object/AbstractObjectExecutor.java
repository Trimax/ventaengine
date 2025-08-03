package io.github.trimax.venta.engine.executors.object;

import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractObjectExecutor extends AbstractExecutor {
    protected AbstractObjectExecutor(@NonNull final InternalVentaContext context,
                                     @NonNull final String command,
                                     @NonNull final String description) {
        super(context, command, description);
    }
}
