package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractSceneExecutor extends AbstractExecutor {
    protected AbstractSceneExecutor(@NonNull final InternalVentaContext context,
                                    @NonNull final String command,
                                    @NonNull final String description) {
        super(context, command, description);
    }
}
