package io.github.trimax.venta.engine.executors.console;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ConsoleClearExecutor extends AbstractConsoleExecutor {
    private ConsoleClearExecutor(@NonNull final InternalVentaContext context) {
        super(context, "clear", "clears console history");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().getHistory().clear();
    }
}
