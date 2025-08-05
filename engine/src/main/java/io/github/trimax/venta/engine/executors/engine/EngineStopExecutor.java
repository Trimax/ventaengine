package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineStopExecutor extends AbstractEngineExecutor {
    private EngineStopExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "stop", "stops the application");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getState().setApplicationRunning(false);
    }
}
