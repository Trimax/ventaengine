package io.github.trimax.venta.engine.executors.system;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractSystemExecutor extends AbstractExecutor {
    protected AbstractSystemExecutor(@NonNull final ControllerFactory factory,
                                     @NonNull final String command,
                                     @NonNull final String description) {
        super(factory, command, description);
    }
}
