package com.venta.engine.managers;

import com.venta.engine.model.core.Couple;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractManager<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>> {
    private final Map<String, Couple<E, V>> values = new ConcurrentHashMap<>();

    final Couple<E, V> get(final String id) {
        return values.get(id);
    }

    protected final String generateID() {
        return UUID.randomUUID().toString();
    }

    protected final V store(final E entity) {
        final var view = createView(generateID(), entity);
        values.put(view.getId(), new Couple<>(entity, view));
        log.debug("{} {} created", view.getClass().getSimpleName(), view.getId());

        return view;
    }

    @SuppressWarnings("unused")
    final void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        values.values().forEach(this::destroy);
        values.clear();
    }

    protected abstract void destroy(final Couple<E, V> couple);

    protected abstract V createView(final String id, final E entity);

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
