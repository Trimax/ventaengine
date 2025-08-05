package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;

public abstract class AbstractSceneExecutor extends AbstractExecutor {
    protected AbstractSceneExecutor(@NonNull final ControllerFactory factory,
                                    @NonNull final String command,
                                    @NonNull final String description) {
        super(factory, command, description);
    }
}
