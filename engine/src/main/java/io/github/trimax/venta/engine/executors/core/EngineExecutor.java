package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.engine.AbstractEngineExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineExecutor extends AbstractCoreExecutor {
    private EngineExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractEngineExecutor> executors) {
        super(context, "engine", "the set of commands to control engine", executors);
    }
}
