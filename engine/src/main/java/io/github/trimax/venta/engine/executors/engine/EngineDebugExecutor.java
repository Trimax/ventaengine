package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineDebugExecutor extends AbstractEngineExecutor {
    private EngineDebugExecutor(@NonNull final InternalVentaContext context) {
        super(context, "debug", "turn on/off the debug mode");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        if (command.asArgument().isBlank()) {
            getConsole().warning("Argument is missing. Please use %s true/false", getCommand());
            return;
        }
        
        getState().setDebugEnabled(command.asArgument().asBoolean());
        getConsole().info("Debug mode: %b", getState().isDebugEnabled());
    }
}
