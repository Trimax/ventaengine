package io.github.trimax.venta.engine.renderers.state;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.model.states.ConsoleState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleStateRenderer extends AbstractStateRenderer<ConsoleState, ConsoleStateRenderer.ConsoleRenderContext, WindowStateRenderer.WindowRenderContext> {
    private final TextStateRenderer consoleItemRenderer;
    private final WindowController windowController;
    private final TextController textController;

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
        glBindVertexArray(console.getVertexArrayObjectID());

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        glBindVertexArray(0);
        glUseProgram(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderHistory(final ConsoleState console) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        final var scaleX = Definitions.CONSOLE_SCALE_HORIZONTAL / windowController.get().getWidth();
        final var scaleY = Definitions.CONSOLE_SCALE_VERTICAL / windowController.get().getHeight();

        for (int line = 0; line < Math.min(windowController.get().getHeight() / Definitions.CONSOLE_LINE_HEIGHT, console.getHistory().size()); line++)
            renderItem(console, line, scaleX, scaleY);

        try (final var _ = consoleItemRenderer.withContext(getContext())
                .withText(new ConsoleController.ConsoleMessage(ConsoleMessageType.Command, console.getBuffer()))
                .withPosition(Definitions.CONSOLE_CHARACTER_WIDTH * scaleX - 1, Definitions.CONSOLE_LINE_HEIGHT * scaleY / 2)
                .withScale(scaleX, scaleY)) {
            consoleItemRenderer.render(textController.get());
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderItem(final ConsoleState console, final int line, final float scaleX, final float scaleY) {
        final var index = console.getHistory().size() - line - 1;
        final var message = (index >= 0 && index < console.getHistory().size()) ? console.getHistory().get(index) : null;
        if (message == null || StringUtils.isBlank(message.text()))
            return;

        try (final var _ = consoleItemRenderer.withContext(getContext())
                .withText(message)
                .withPosition(Definitions.CONSOLE_CHARACTER_WIDTH * scaleX - 1, Definitions.CONSOLE_LINE_INTERVAL * Definitions.CONSOLE_LINE_HEIGHT * scaleY * (line + 2))
                .withScale(scaleX, scaleY)) {
            consoleItemRenderer.render(textController.get());
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
