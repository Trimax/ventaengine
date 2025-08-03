package io.github.trimax.venta.engine.executors.system;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SystemTimeExecutor extends AbstractSystemExecutor {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private SystemTimeExecutor(@NonNull final InternalVentaContext context) {
        super(context, "time", "prints current date and time");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().info("Current time: %s", LocalDateTime.now().format(formatter));
    }
}
