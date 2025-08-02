package io.github.trimax.venta.engine.executors.core;

import java.util.List;

import io.github.trimax.venta.engine.core.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractCoreExecutor extends AbstractExecutor {
    protected AbstractCoreExecutor(@NonNull final InternalVentaContext context, @NonNull final String command, @NonNull final String description, final List<? extends AbstractExecutor> executors) {
        super(context, command, description, null, executors);
    }
}
