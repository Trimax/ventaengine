package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.engine.managers.AbstractManager;
import io.github.trimax.venta.engine.model.view.AbstractView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractManagerImplementation<E extends V, V extends AbstractView> implements AbstractManager<V> {
    private final Map<String, E> values = new ConcurrentHashMap<>();
    private final Map<String, String> cache = new HashMap<>();

    @Override
    public final V get(@NonNull final String id) {
        return getEntity(id);
    }

    @Override
    public final Iterator<V> iterator() {
        return StreamEx.ofValues(values).map(this::toView).iterator();
    }

    public final Iterator<E> entityIterator() {
        return StreamEx.ofValues(values).iterator();
    }

    protected final boolean isCached(final String name) {
        return shouldCache() && cache.containsKey(name);
    }

    protected final E getCached(final String name) {
        return values.get(cache.get(name));
    }

    public final E getEntity(final String id) {
        return values.get(id);
    }

    protected final E store(final E entity) {
        if (shouldCache())
            cache.put(entity.getName(), entity.getID());

        values.put(entity.getID(), entity);
        log.debug("{} {} created", entity.getClass().getSimpleName(), entity.getID());

        return entity;
    }

    public final void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        values.values().forEach(this::destroy);
        values.clear();
    }

    private V toView(final E entity) {
        return entity;
    }

    protected abstract void destroy(final E entity);

    protected abstract boolean shouldCache();

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract static class AbstractEntity implements AbstractView {
        private final String id = UUID.randomUUID().toString();
        private final GizmoManagerImplementation.GizmoEntity gizmo;
        private final String name;

        protected AbstractEntity(final String name) {
            this(null, name);
        }

        @Override
        public final String getID() {
            return id;
        }

        @Override
        public final String getName() {
            return name;
        }

        public final GizmoManagerImplementation.GizmoEntity getGizmo() {
            return gizmo;
        }
    }
}
