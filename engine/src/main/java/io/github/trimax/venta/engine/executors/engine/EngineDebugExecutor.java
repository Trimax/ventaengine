package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineDebugExecutor extends AbstractEngineExecutor {
    private EngineDebugExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "debug", "turns on/off the debug mode");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <true/false>", command.getFullPath());
            return;
        }

        getState().setDebugEnabled(command.asArgument().asBoolean());
        getConsole().info("Debug mode: %b", getState().isDebugEnabled());
    }
}
