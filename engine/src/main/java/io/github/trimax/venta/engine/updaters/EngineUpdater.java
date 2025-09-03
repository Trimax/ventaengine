package io.github.trimax.venta.engine.updaters;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineUpdater {
    private final SceneManagerImplementation sceneManager;
    private final SceneUpdater sceneUpdater;

    public void update(final Engine.VentaTime time) {
        sceneUpdater.update(sceneManager.getCurrent(), (float) time.getDelta());
    }
}
