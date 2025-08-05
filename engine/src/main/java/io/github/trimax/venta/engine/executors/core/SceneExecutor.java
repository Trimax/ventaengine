package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.scene.AbstractSceneExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneExecutor extends AbstractCoreExecutor {
    private SceneExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractSceneExecutor> executors) {
        super(factory, "scene", "the set of commands to manage scenes", executors);
    }
}
