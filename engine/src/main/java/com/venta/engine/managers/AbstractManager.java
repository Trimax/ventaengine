package com.venta.engine.managers;

import com.venta.engine.model.core.Couple;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractManager<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>> {
    private final Map<Long, Couple<E, V>> values = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    public final V get(final Long id) {
        return values.get(id).view();
    }

    protected final long generateID() {
        return counter.incrementAndGet();
    }

    protected final V store(final E entity, final V view) {
        values.put(entity.getId(), new Couple<>(entity, view));
        return view;
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
