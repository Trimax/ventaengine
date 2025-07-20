package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.view.SceneView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneManager extends AbstractManager<SceneManager.SceneEntity, SceneView> {
    @Getter
    @Setter
    private SceneView current;

    public SceneView create(final String name) {
        log.info("Creating scene {}", name);

        final var entity = new SceneEntity(name);
        return store(entity);
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
        private final List<ObjectManager.ObjectEntity> objects;

        SceneEntity(@NonNull final String name) {
            super(0L);

            this.name = name;
            this.objects = new ArrayList<>();
        }
    }
}
