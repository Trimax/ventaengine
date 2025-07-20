package com.venta.engine.renderers;

import com.venta.engine.managers.AbstractManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public interface AbstractRenderer<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>> {
    void render(final V view);

    @AllArgsConstructor
    abstract class AbstractView<E extends AbstractManager.AbstractEntity> {
        @NonNull
        protected final E entity;

        public final long getId() {
            return entity.getId();
        }
    }
}
