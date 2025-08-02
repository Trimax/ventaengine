package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineStopExecutor extends AbstractEngineExecutor {
    private EngineStopExecutor(@NonNull final InternalVentaContext context) {
        super(context, "stop", "stops the application");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        getState().setApplicationRunning(false);
    }

    @Override
    public String getCommand() {
        return "stop";
    }
}
