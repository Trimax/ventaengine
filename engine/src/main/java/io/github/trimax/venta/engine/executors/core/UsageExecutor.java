package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.engine.AbstractEngineExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class UsageExecutor extends AbstractCoreExecutor {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private UsageExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractEngineExecutor> executors) {
        super(context, "usage", "prints resources utilization", executors);
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        getConsole().info("Current time: %s", LocalDateTime.now().format(formatter));
    }
}
