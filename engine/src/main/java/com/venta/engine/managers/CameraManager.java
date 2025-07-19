package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class CameraManager extends AbstractManager<CameraManager.CameraEntity> {
    public CameraEntity create(final String name) {
        log.info("Creating camera {}", name);

        return store(new CameraEntity(generateID(), name));
    }

    @Override
    protected void destroy(final CameraEntity object) {
        log.info("Deleting camera {}", object.getName());
    }

    @Getter
    public static final class CameraEntity extends AbstractEntity {
        private final String name;

        CameraEntity(final long id, @NonNull final String name) {
            super(id);

            this.name = name;
        }
    }
}
