package com.venta.engine.renderers;

import com.venta.engine.managers.AbstractManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRenderer<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>,
        C extends AbstractRenderer.AbstractRenderContext> implements AutoCloseable {
    @Getter
    private final C context;

    public AbstractRenderer() {
        context = createContext();
    }

    protected abstract C createContext();

    abstract void render(final V view);

    @AllArgsConstructor
    public abstract static class AbstractView<E extends AbstractManager.AbstractEntity> {
        @Getter
        @NonNull
        private final String id;

        @NonNull
        protected final E entity;
    }

    @Getter(AccessLevel.PACKAGE)
    abstract static class AbstractRenderContext implements AutoCloseable {
        public abstract void destroy();
    }

    @Override
    public final void close() {
        context.destroy();
    }
}
