package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.object.AbstractObjectExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ObjectExecutor extends AbstractCoreExecutor {
    private ObjectExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractObjectExecutor> executors) {
        super(factory, "object", "the set of commands to manage objects", executors);
    }
}
