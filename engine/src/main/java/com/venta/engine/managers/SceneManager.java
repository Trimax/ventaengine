package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneManager extends AbstractManager<SceneManager.SceneEntity, SceneView> {
    @Getter
    @Setter
    private SceneView current;

    public SceneView create(final String name) {
        log.info("Creating scene {}", name);

        return store(new SceneEntity(name));
    }

    @Override
    protected SceneView createView(final String id, final SceneEntity entity) {
        return new SceneView(id, entity);
    }

    @Override
    protected void destroy(final Couple<SceneEntity, SceneView> scene) {
        log.info("Deleting scene {}", scene.entity().getName());
    }

    @Getter
    public static final class SceneEntity extends AbstractEntity {
        private final String name;

        SceneEntity(@NonNull final String name) {
            this.name = name;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class SceneAccessor extends AbstractAccessor {}
}
