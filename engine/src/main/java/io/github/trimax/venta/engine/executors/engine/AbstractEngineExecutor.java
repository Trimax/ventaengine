package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.engine.core.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractEngineExecutor extends AbstractExecutor {
    protected AbstractEngineExecutor(@NonNull final InternalVentaContext context, @NonNull final String command) {
        super(context, command);
    }
}
