package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.ConsoleItemManager;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.model.view.ConsoleItemView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleItemRenderer extends AbstractRenderer<ConsoleItemView, ConsoleItemRenderer.ConsoleItemRenderContext, ConsoleRenderer.ConsoleRenderContext> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ProgramManager programManager;

    @Override
    protected ConsoleItemRenderContext createContext() {
        return new ConsoleItemRenderContext();
    }

    @Override
    void render(final ConsoleItemView consoleItem) {
        if (consoleItem instanceof ConsoleItemManager.ConsoleItemEntity entity)
            render(entity);
    }

    private void render(final ConsoleItemManager.ConsoleItemEntity consoleItem) {

    }

    @Getter(AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ConsoleItemRenderContext extends AbstractRenderContext<ConsoleRenderer.ConsoleRenderContext> {
        @Override
        public void close() {
        }

        @Override
        public void destroy() {
        }
    }
}
