package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.model.entity.ConsoleEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleRenderer extends AbstractRenderer<ConsoleEntity, ConsoleRenderer.ConsoleRenderContext, WindowRenderer.WindowRenderContext> {
    private final ConsoleItemRenderer consoleItemRenderer;

    @Override
    protected ConsoleRenderContext createContext() {
        return new ConsoleRenderContext();
    }

    @Override
    void render(final ConsoleEntity console) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        renderBackground(console);
        renderHistory(console);
    }

    private void renderBackground(final ConsoleEntity console) {
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

    private void renderHistory(final ConsoleEntity console) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (int line = 0; line < Math.min(Definitions.CONSOLE_HISTORY_LINES_COUNT, console.getHistory().size()); line++)
            renderItem(console, line);

        try (final var _ = consoleItemRenderer.withContext(getContext())
                .withText(new ConsoleController.ConsoleMessage(ConsoleMessageType.Command, console.getInputBuffer().toString()))
                .withPosition(-0.98f, 0.05f)
                .withScale(0.001f)) {
            consoleItemRenderer.render(console.getConsoleItem());
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderItem(final ConsoleEntity console, final int line) {
        final var index = console.getHistory().size() - line - 1;
        final var message = (index >= 0 && index < console.getHistory().size()) ? console.getHistory().get(index) : null;
        if (message == null || StringUtils.isBlank(message.text()))
            return;

        try (final var _ = consoleItemRenderer.withContext(getContext())
                .withText(message)
                .withPosition(-0.98f, 0.1f + Definitions.CONSOLE_LINE_HEIGHT * line)
                .withScale(0.001f)) {
            consoleItemRenderer.render(console.getConsoleItem());
        }
    }

    @Getter(AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ConsoleRenderContext extends AbstractRenderContext<WindowRenderer.WindowRenderContext> {
        @Override
        public void close() {
        }

        @Override
        public void destroy() {
        }
    }
}
