package com.venta.engine.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.venta.engine.model.core.Couple;
import com.venta.engine.model.views.AbstractView;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractManager<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>> {
    private final Map<String, Couple<E, V>> values = new ConcurrentHashMap<>();

    final Couple<E, V> get(final String id) {
        return values.get(id);
    }

    protected final V store(final E entity) {
        final var view = createView(entity);
        values.put(entity.getID(), new Couple<>(entity, view));
        log.debug("{} {} created", view.getClass().getSimpleName(), entity.getID());

        return view;
    }

    @SuppressWarnings("unused")
    final void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        values.values().forEach(this::destroy);
        values.clear();
    }

    protected abstract void destroy(final Couple<E, V> couple);

    protected abstract V createView(final E entity);

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract static class AbstractEntity implements AbstractView {
        private final String id = UUID.randomUUID().toString();

        @Override
        public final String getID() {
            return id;
        }
    }

    abstract class AbstractAccessor {
        public final E getByID(final String id) {
            return values.get(id).entity();
        }
    }
}
