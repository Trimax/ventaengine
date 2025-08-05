package io.github.trimax.venta.engine.renderers.state;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.renderers.ConsoleRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowRenderer extends AbstractStateRenderer<WindowState, WindowRenderer.WindowRenderContext, WindowRenderer.WindowRenderContext> {
    private final ConsoleController consoleController;
    private final ConsoleRenderer consoleRenderer;
    private long lastUpdated = 0;

    @Override
    protected WindowRenderContext createContext() {
        return new WindowRenderContext();
    }

    @Override
    public void render(final WindowState window) {
        if (consoleController.get().isVisible())
            try (final var _ = consoleRenderer.withContext(getContext())) {
                consoleRenderer.render(consoleController.get());
            }

        glfwSwapBuffers(window.getInternalID());

        final var now = System.currentTimeMillis();
        if (now - lastUpdated >= 1000) {
            lastUpdated = now;
            glfwSetWindowTitle(window.getInternalID(), window.getTitle() + ": " + getContext().getFrameRate());
        }
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class WindowRenderContext extends AbstractRenderContext<WindowRenderContext> {
        private int frameRate;

        public WindowRenderContext withFrameRate(final int frameRate) {
            this.frameRate = frameRate;

            return this;
        }

        @Override
        public void close() {
        }

        @Override
        public void destroy() {

        }
    }
}
