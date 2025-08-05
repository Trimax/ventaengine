package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.engine.AbstractEngineExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineExecutor extends AbstractCoreExecutor {
    private EngineExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractEngineExecutor> executors) {
        super(factory, "engine", "the set of commands to control engine", executors);
    }
}
