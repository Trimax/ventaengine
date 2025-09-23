package io.github.trimax.venta.engine.renderers.state;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import org.apache.commons.lang3.StringUtils;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.definitions.DefinitionsConsole;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.states.ConsoleState;
import io.github.trimax.venta.engine.model.states.WindowState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

        final var consoleHeight = windowController.get().getHeight() / 2;
        final var countVisibleLines = Math.min(consoleHeight / getScaledLineHeight(), console.getHistory().size());
        for (int line = 0; line < countVisibleLines; line++)
            renderItem(console, line, windowController.get());

        try (final var _ = textStateRenderer.withContext(getContext())
                .withScale(DefinitionsConsole.CONSOLE_TEXT_SCALE)
                .withText(new ConsoleController.ConsoleMessage(ConsoleMessageType.Command, console.getBuffer()))
                .withWindow(windowController.get().getWidth(), windowController.get().getHeight())
                .withPosition(getScaledMarginLeft(), consoleHeight - getScaledMarginBottom() * DefinitionsConsole.CONSOLE_TEXT_SCALE)) {
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

        final var consoleHeight = window.getHeight() / 2;
        try (final var _ = textStateRenderer.withContext(getContext())
                .withText(message)
                .withScale(DefinitionsConsole.CONSOLE_TEXT_SCALE)
                .withWindow(window.getWidth(), window.getHeight())
                .withPosition(getScaledMarginLeft(), consoleHeight - getScaledMarginBottom() - (line + 1) * getScaledLineHeight())) {
            textStateRenderer.render(textController.get());
        }
    }

    private float getScaledLineHeight() {
        return DefinitionsConsole.CONSOLE_LINE_HEIGHT * DefinitionsConsole.CONSOLE_TEXT_SCALE * DefinitionsConsole.CONSOLE_LINE_INTERVAL;
    }

    private float getScaledMarginLeft() {
        return DefinitionsConsole.CONSOLE_MARGIN_LEFT * DefinitionsConsole.CONSOLE_TEXT_SCALE;
    }

    private float getScaledMarginBottom() {
        return DefinitionsConsole.CONSOLE_MARGIN_BOTTOM * DefinitionsConsole.CONSOLE_TEXT_SCALE;
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
