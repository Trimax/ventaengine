package io.github.trimax.venta.engine.executors.console;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractConsoleExecutor extends AbstractExecutor {
    protected AbstractConsoleExecutor(@NonNull final ControllerFactory factory,
                                      @NonNull final String command,
                                      @NonNull final String description) {
        super(factory, command, description);
    }
}
