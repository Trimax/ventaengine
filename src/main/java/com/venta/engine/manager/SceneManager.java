package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneManager extends AbstractManager<SceneManager.SceneEntity> {
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
