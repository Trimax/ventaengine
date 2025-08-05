package io.github.trimax.venta.engine.executors.console;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ConsoleInfoExecutor extends AbstractConsoleExecutor {
    private ConsoleInfoExecutor(@NonNull final InternalVentaContext context) {
        super(context, "info", "prints information about the console");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Console information:");
        getConsole().info("Commands executed: %d", getConsole().get().getCommands().size());
        getConsole().info("     History size: %d", getConsole().get().getHistory().size());
    }
}
