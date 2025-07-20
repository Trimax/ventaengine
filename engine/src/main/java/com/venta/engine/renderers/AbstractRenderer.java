package com.venta.engine.renderers;

import com.venta.engine.managers.AbstractManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public interface AbstractRenderer<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>> {
    void render(final V view);

    @AllArgsConstructor
    abstract class AbstractView<E extends AbstractManager.AbstractEntity> {
        @Getter
        @NonNull
        private final String id;

        @NonNull
        protected final E entity;
    }
}
