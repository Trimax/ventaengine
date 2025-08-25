package io.github.trimax.venta.engine.executors.material;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractMaterialExecutor extends AbstractExecutor {
    protected AbstractMaterialExecutor(@NonNull final ControllerFactory factory,
                                       @NonNull final String command,
                                       @NonNull final String description) {
        super(factory, command, description);
    }
}
