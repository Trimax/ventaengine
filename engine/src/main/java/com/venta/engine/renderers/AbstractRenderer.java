package com.venta.engine.renderers;

import com.venta.engine.managers.AbstractManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

public abstract class AbstractRenderer<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>,
        C extends AbstractRenderer.AbstractRenderContext> {
    @Getter(AccessLevel.PROTECTED)
    private C context;

    abstract void render(final V view);

    @AllArgsConstructor
    public abstract static class AbstractView<E extends AbstractManager.AbstractEntity> {
        @Getter
        @NonNull
        private final String id;

        @NonNull
        protected final E entity;
    }

    @SuperBuilder
    @Getter(AccessLevel.PACKAGE)
    abstract static class AbstractRenderContext implements AutoCloseable {

    }

    final AutoCloseable withContext(@NonNull final C context) {
        this.context = context;

        return () -> {
            context.close();
            this.context = null;
        };
    }
}
