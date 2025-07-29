package com.venta.engine.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.venta.engine.model.view.AbstractView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractManager<E extends V, V extends AbstractView> {
    private final Map<String, E> values = new ConcurrentHashMap<>();
    private final Map<String, String> cache = new HashMap<>();

    protected final boolean isCached(final String name) {
        return cache.containsKey(name);
    }

    protected final E getCached(final String name) {
        return values.get(cache.get(name));
    }

    protected final E get(final String id) {
        return values.get(id);
    }

    protected final V store(final E entity) {
        values.put(entity.getID(), entity);
        cache.put(entity.getName(), entity.getID());
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
        private final String name;

        @Override
        public final String getID() {
            return id;
        }

        @Override
        public final String getName() {
            return name;
        }
    }

    abstract class AbstractAccessor {
        public final E get(final String id) {
            return values.get(id);
        }

        public final E get(final V view) {
            return view == null ? null : get(view.getID());
        }

        public final Iterator<E> iterator() {
            return values.values().iterator();
        }

        public final void cleanup() {
            AbstractManager.this.cleanup();
        }
    }
}
