package com.venta.engine.managers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractManager<V extends AbstractManager.AbstractEntity> {
    private final Map<Long, V> values = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    public final V get(final Long id) {
        return values.get(id);
    }

    protected final long generateID() {
        return counter.incrementAndGet();
    }

    protected final V store(final V value) {
        values.put(value.getId(), value);
        return value;
    }

    final void destroy() {
        values.values().forEach(this::destroy);
        values.clear();
    }

    protected abstract void destroy(final V value);

    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract static class AbstractEntity {
        @NonNull
        private final Long id;

        public final int getIdAsInteger() {
            return id.intValue();
        }
    }
}
