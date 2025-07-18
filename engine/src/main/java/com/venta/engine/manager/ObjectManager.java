package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.VentaObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ObjectManager extends AbstractManager<ObjectManager.ObjectEntity> {
    private final ResourceManager resourceManager;

    public ObjectEntity load(final String name) {
        log.info("Loading object {}", name);

        return store(new ObjectEntity(generateID(), name, resourceManager.load(String.format("/objects/%s", name), VentaObject.class)));
    }

    @Override
    protected void destroy(final ObjectEntity object) {
        log.info("Unloading object {}", object.getName());
    }

    @Getter
    public static final class ObjectEntity extends AbstractEntity {
        private final String name;
        private final VentaObject object;

        ObjectEntity(final long id, @NonNull final String name, @NonNull final VentaObject object) {
            super(id);

            this.name = name;
            this.object = object;
        }
    }
}
