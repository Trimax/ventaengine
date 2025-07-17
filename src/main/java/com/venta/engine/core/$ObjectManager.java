package com.venta.engine.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venta.engine.model.VentaObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@AllArgsConstructor
public final class $ObjectManager extends $AbstractManager<$ObjectManager.ObjectEntity> {
    final static $ObjectManager instance = new $ObjectManager();
    private static final Gson parser = new GsonBuilder().create();
    private static final AtomicLong counter = new AtomicLong();

    public ObjectEntity load(final String name) {
        log.info("Loading object {}", name);

        return store(new ObjectEntity(counter.incrementAndGet(), name,
                parse($ResourceManager.instance.load(String.format("/objects/%s", name)))));
    }

    private VentaObject parse(final String serializedObject) {
        return parser.fromJson(serializedObject, VentaObject.class);
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
