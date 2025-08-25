package io.github.trimax.venta.engine.executors.light;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractLightExecutor extends AbstractExecutor {
    protected AbstractLightExecutor(@NonNull final ControllerFactory factory,
                                    @NonNull final String command,
                                    @NonNull final String description) {
        super(factory, command, description);
    }
}
