package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

import java.util.List;

public abstract class AbstractCoreExecutor extends AbstractExecutor {
    protected AbstractCoreExecutor(@NonNull final ControllerFactory factory,
                                   @NonNull final String command,
                                   @NonNull final String description,
                                   final List<? extends AbstractExecutor> executors) {
        super(factory, command, description, executors);
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        delegateExecution(command);
    }
}
