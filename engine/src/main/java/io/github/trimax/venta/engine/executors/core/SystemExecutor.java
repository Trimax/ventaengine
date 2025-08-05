package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.system.AbstractSystemExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SystemExecutor extends AbstractCoreExecutor {
    private SystemExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractSystemExecutor> executors) {
        super(factory, "system", "the set of commands to manage system", executors);
    }
}
