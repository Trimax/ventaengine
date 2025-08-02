package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.model.entities.SceneEntity;
import io.github.trimax.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneManagerImplementation
        extends AbstractManagerImplementation<SceneEntity, SceneView>
        implements SceneManager {
    @Getter
    private SceneEntity current;

    @Override
    public SceneEntity create(@NonNull final String name) {
        log.info("Creating scene {}", name);

        return store(new SceneEntity(name));
    }

    @Override
    public void setCurrent(@NonNull final SceneView scene) {
        if (scene instanceof SceneEntity entity)
            this.current = entity;
    }

    @Override
    protected void destroy(final SceneEntity scene) {
        log.info("Destroying scene {} ({})", scene.getID(), scene.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
