package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

abstract class AbstractRenderer<E extends AbstractEntity, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> implements AutoCloseable {
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

    abstract void render(final E view);

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
