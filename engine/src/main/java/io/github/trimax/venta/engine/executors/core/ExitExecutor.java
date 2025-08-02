package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.engine.AbstractEngineExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ExitExecutor extends AbstractCoreExecutor {
    private ExitExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractEngineExecutor> executors) {
        super(context, "exit", "stops the application", executors);
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        getState().setApplicationRunning(false);
    }
}
