package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.mesh.AbstractMeshExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MeshExecutor extends AbstractCoreExecutor {
    private MeshExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractMeshExecutor> executors) {
        super(context, "mesh", "the set of commands to manage meshes", executors);
    }
}
