package io.github.trimax.venta.engine.executors.object;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractObjectExecutor extends AbstractExecutor {
    protected AbstractObjectExecutor(@NonNull final ControllerFactory factory,
                                     @NonNull final String command,
                                     @NonNull final String description) {
        super(factory, command, description);
    }
}
