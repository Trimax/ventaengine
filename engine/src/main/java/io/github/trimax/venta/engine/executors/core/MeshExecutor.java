package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.mesh.AbstractMeshExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MeshExecutor extends AbstractCoreExecutor {
    private MeshExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractMeshExecutor> executors) {
        super(factory, "mesh", "the set of commands to manage meshes", executors);
    }
}
