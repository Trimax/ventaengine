package com.venta.engine.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.venta.engine.model.views.AbstractView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractManager<E extends V, V extends AbstractView> {
    private final Map<String, E> values = new ConcurrentHashMap<>();

    protected final E get(final String id) {
        return values.get(id);
    }

    protected final V store(final E entity) {
        values.put(entity.getID(), entity);
        log.debug("{} {} created", entity.getClass().getSimpleName(), entity.getID());

        return entity;
    }

    private void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        values.values().forEach(this::destroy);
        values.clear();
    }

    protected abstract void destroy(final E entity);

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract static class AbstractEntity implements AbstractView {
        private final String id = UUID.randomUUID().toString();

        @Override
        public final String getID() {
            return id;
        }
    }

    abstract class AbstractAccessor {
        public final E get(final String id) {
            return values.get(id);
        }

        public final E get(final V view) {
            return get(view.getID());
        }

        public void cleanup() {
            AbstractManager.this.cleanup();
        }
    }
}
