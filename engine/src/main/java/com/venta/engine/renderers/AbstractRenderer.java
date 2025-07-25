package com.venta.engine.renderers;

import com.venta.engine.managers.AbstractManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

public abstract class AbstractRenderer<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>,
        C extends AbstractRenderer.AbstractRenderContext<P>, P extends AbstractRenderer.AbstractRenderContext<?>> implements AutoCloseable {
    @Getter(AccessLevel.PROTECTED)
    private final C context;

    public final C withContext(final P parentContext) {
        context.setParent(parentContext);
        return context;
    }

    public AbstractRenderer() {
        context = createContext();
    }

    protected abstract C createContext();

    abstract void render(final V view);

    @AllArgsConstructor
    public abstract static class AbstractView<E extends AbstractManager.AbstractEntity> {
        @NonNull
        protected final E entity;

        public final String getID() {
            return entity.getID();
        }
    }

    @Slf4j
    @NoArgsConstructor
    @Getter(AccessLevel.PACKAGE)
    abstract static class AbstractRenderContext<C extends AbstractRenderContext<?>> implements AutoCloseable {
        @Setter(AccessLevel.PACKAGE)
        private C parent;

        public abstract void destroy();
    }

    @Override
    public final void close() {
        AbstractRenderContext.log.debug("{} destroyed", getClass().getSimpleName());
        context.destroy();
    }
}
