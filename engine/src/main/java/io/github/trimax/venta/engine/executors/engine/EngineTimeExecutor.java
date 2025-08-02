package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineTimeExecutor extends AbstractEngineExecutor {
    private final long timeStarted = System.nanoTime();

    private EngineTimeExecutor(@NonNull final InternalVentaContext context) {
        super(context, "time", "shows the number of seconds the engine is running");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        getConsole().info("Engine works for %.3fs", (System.nanoTime() - timeStarted) / 1e9);
    }
}
