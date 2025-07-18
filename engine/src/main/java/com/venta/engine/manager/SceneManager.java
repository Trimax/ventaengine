package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneManager extends AbstractManager<SceneManager.SceneEntity> {
    @Getter
    @Setter
    private SceneEntity current;

    public SceneEntity create(final String name) {
        log.info("Creating scene {}", name);

        return store(new SceneEntity(generateID(), name));
    }

    @Override
    protected void destroy(final SceneEntity object) {
        log.info("Unloading scene {}", object.getName());
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
