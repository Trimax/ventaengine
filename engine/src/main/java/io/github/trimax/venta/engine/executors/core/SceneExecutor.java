package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.scene.AbstractSceneExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneExecutor extends AbstractCoreExecutor {
    private SceneExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractSceneExecutor> executors) {
        super(context, "scene", "the set of commands to manage scenes", executors);
    }
}
