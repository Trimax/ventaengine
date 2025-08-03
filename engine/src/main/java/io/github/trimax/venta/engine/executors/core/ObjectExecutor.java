package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.object.AbstractObjectExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ObjectExecutor extends AbstractCoreExecutor {
    private ObjectExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractObjectExecutor> executors) {
        super(context, "object", "the set of commands to manage objects", executors);
    }
}
