package io.github.trimax.venta.engine.renderers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRenderer<O,
        C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>>
        implements AutoCloseable {
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

    public abstract void render(final O object);

    @Getter
    @NoArgsConstructor
    @Setter(AccessLevel.PROTECTED)
    public abstract static class AbstractRenderContext<C extends AbstractRenderContext<?>> implements AutoCloseable {
        private C parent;

        public abstract void destroy();
    }

    @Override
    public final void close() {
        log.debug("{} destroyed", getClass().getSimpleName());
        context.destroy();
    }
}
