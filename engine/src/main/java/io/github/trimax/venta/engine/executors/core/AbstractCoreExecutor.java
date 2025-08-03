package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

import java.util.List;

public abstract class AbstractCoreExecutor extends AbstractExecutor {
    protected AbstractCoreExecutor(@NonNull final InternalVentaContext context, @NonNull final String command, @NonNull final String description, final List<? extends AbstractExecutor> executors) {
        super(context, command, description, executors);
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        delegateExecution(command);
    }
}
