package com.venta.engine.managers;

import com.venta.engine.model.core.Couple;
import com.venta.engine.model.view.AbstractView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractManager<E extends AbstractManager.AbstractEntity, V extends AbstractView> {
    private final Map<Long, Couple<E, V>> values = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    public final E get(final Long id) {
        return values.get(id).entity();
    }

    protected final long generateID() {
        return counter.incrementAndGet();
    }

    protected final E store(final E value) {
        values.put(value.getId(), new Couple<>(value, null));
        return value;
    }

    final void destroy() {
        StreamEx.of(values.values()).map(Couple::entity).forEach(this::destroy);
        values.clear();
    }

    protected abstract void destroy(final E value);

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
