package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractMeshExecutor extends AbstractExecutor {
    protected AbstractMeshExecutor(@NonNull final ControllerFactory factory,
                                   @NonNull final String command,
                                   @NonNull final String description) {
        super(factory, command, description);
    }
}
