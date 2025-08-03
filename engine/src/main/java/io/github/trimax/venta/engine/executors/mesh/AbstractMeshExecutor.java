package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.NonNull;

public abstract class AbstractMeshExecutor extends AbstractExecutor {
    protected AbstractMeshExecutor(@NonNull final InternalVentaContext context,
                                   @NonNull final String command,
                                   @NonNull final String description) {
        super(context, command, description);
    }
}
