package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.engine.managers.AbstractManager;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractManagerImplementation<T extends I, I extends AbstractInstance> implements AbstractManager<I> {
    private final Map<String, T> values = new ConcurrentHashMap<>();
    private final Map<String, String> cache = new HashMap<>();

    @Override
    public final I get(@NonNull final String id) {
        return getInstance(id);
    }

    @Override
    public final Iterator<I> iterator() {
        return StreamEx.ofValues(values).map(this::toView).iterator();
    }

    public final Iterator<T> instanceIterator() {
        return StreamEx.ofValues(values).iterator();
    }

    protected final boolean isCached(final String name) {
        return shouldCache() && cache.containsKey(name);
    }

    protected final T getCached(final String name) {
        return values.get(cache.get(name));
    }

    public final T getInstance(final String id) {
        return values.get(id);
    }

    protected final T store(final T entity) {
        if (shouldCache())
            cache.put(entity.getName(), entity.getID());

        values.put(entity.getID(), entity);
        log.debug("{} {} created", entity.getClass().getSimpleName(), entity.getID());

        return entity;
    }

    protected final void delete(final T entity) {
        cache.remove(entity.getName());
        values.remove(entity.getID());
        destroy(entity);
        log.debug("{} {} deleted", entity.getClass().getSimpleName(), entity.getID());
    }

    public final void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        values.values().forEach(this::destroy);
        values.clear();
    }

    private I toView(final T entity) {
        return entity;
    }

    protected abstract void destroy(final T entity);

    protected abstract boolean shouldCache();
}
