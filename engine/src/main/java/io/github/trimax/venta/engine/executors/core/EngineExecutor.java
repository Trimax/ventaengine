package io.github.trimax.venta.engine.executors.core;

import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import io.github.trimax.venta.engine.executors.engine.AbstractEngineExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineExecutor extends AbstractCoreExecutor {
    private EngineExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractEngineExecutor> executors) {
        super(context, "engine", "the set of commands to control engine", executors);
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        delegateExecution(command);
    }
}
