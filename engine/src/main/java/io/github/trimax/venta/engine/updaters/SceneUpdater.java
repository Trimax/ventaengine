package io.github.trimax.venta.engine.updaters;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneUpdater implements AbstractUpdater<SceneInstanceImplementation> {
    private final EmitterUpdater emitterUpdater;

    @Override
    public void update(final SceneInstanceImplementation scene, final float delta) {
        StreamEx.of(scene.getEmitters()).forEach(emitter -> emitterUpdater.update(emitter, delta));
    }
}
