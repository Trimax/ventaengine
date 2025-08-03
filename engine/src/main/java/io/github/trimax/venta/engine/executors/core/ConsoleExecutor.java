package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.console.AbstractConsoleExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ConsoleExecutor extends AbstractCoreExecutor {
    private ConsoleExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractConsoleExecutor> executors) {
        super(context, "console", "the set of commands to manage console", executors);
    }
}
