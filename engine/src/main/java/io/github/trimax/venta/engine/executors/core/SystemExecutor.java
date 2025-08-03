package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.system.AbstractSystemExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SystemExecutor extends AbstractCoreExecutor {
    private SystemExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractSystemExecutor> executors) {
        super(context, "system", "the set of commands to manage system", executors);
    }
}
