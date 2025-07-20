package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
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

        final var entity = new SceneEntity(generateID(), name);
        return store(entity, new SceneView(entity));
    }

    @Override
    protected void destroy(final SceneEntity object) {
        log.info("Deleting scene {}", object.getName());
    }

    @Getter
    public static final class SceneEntity extends AbstractEntity {
        private final String name;
        private final List<ObjectManager.ObjectEntity> objects;

        SceneEntity(final long id, @NonNull final String name) {
            super(id);

            this.name = name;
            this.objects = new ArrayList<>();
        }
    }
}
