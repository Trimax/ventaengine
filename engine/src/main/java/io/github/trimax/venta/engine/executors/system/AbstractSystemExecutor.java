package io.github.trimax.venta.engine.executors.system;

import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractSystemExecutor extends AbstractExecutor {
    protected AbstractSystemExecutor(@NonNull final InternalVentaContext context,
                                     @NonNull final String command,
                                     @NonNull final String description) {
        super(context, command, description);
    }
}
