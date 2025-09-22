package io.github.trimax.venta.engine.renderers.state;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.states.ConsoleState;
import io.github.trimax.venta.engine.model.states.WindowState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleStateRenderer extends AbstractStateRenderer<ConsoleState, ConsoleStateRenderer.ConsoleRenderContext, WindowStateRenderer.WindowRenderContext> {
    private final TextStateRenderer textStateRenderer;
    private final WindowController windowController;
    private final TextController textController;
    private final GeometryHelper geometryHelper;

    @Override
    protected ConsoleRenderContext createContext() {
        return new ConsoleRenderContext();
    }

    @Override
    public void render(final ConsoleState console) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        renderBackground(console);
        renderHistory(console);
    }

    private void renderBackground(final ConsoleState console) {
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(console.getProgram().getInternalID());
        geometryHelper.render(console.getGeometry());
        glUseProgram(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderHistory(final ConsoleState console) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (int line = 0; line < Math.min(windowController.get().getHeight() / Definitions.CONSOLE_LINE_HEIGHT, console.getHistory().size()); line++)
            renderItem(console, line, windowController.get());

        try (final var _ = textStateRenderer.withContext(getContext())
                .withText(new ConsoleController.ConsoleMessage(ConsoleMessageType.Command, console.getBuffer()))
                .withWindow(windowController.get().getWidth(), windowController.get().getHeight())
                .withPosition(Definitions.CONSOLE_MARGIN, windowController.get().getHeight() / 2.f - Definitions.CONSOLE_MARGIN)) {
            textStateRenderer.render(textController.get());
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderItem(final ConsoleState console, final int line, final WindowState window) {
        final var index = console.getHistory().size() - line - 1;
        final var message = (index >= 0 && index < console.getHistory().size()) ? console.getHistory().get(index) : null;
        if (message == null || StringUtils.isBlank(message.text()))
            return;

        try (final var _ = textStateRenderer.withContext(getContext())
                .withText(message)
                .withWindow(window.getWidth(), window.getHeight())
                .withPosition(Definitions.CONSOLE_MARGIN, window.getHeight() / 2.f - Definitions.CONSOLE_MARGIN - (line + 2) * Definitions.CONSOLE_LINE_HEIGHT)) {
            textStateRenderer.render(textController.get());
        }
    }

    @Getter(AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ConsoleRenderContext extends AbstractRenderContext<WindowStateRenderer.WindowRenderContext> {
        @Override
        public void close() {
        }

        @Override
        public void destroy() {
        }
    }
}
