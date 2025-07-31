package io.github.trimax.venta.engine.renderers;

import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.ConsoleManager;
import io.github.trimax.venta.engine.managers.WindowManager;
import io.github.trimax.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowRenderer extends AbstractRenderer<WindowView, WindowRenderer.WindowRenderContext, WindowRenderer.WindowRenderContext> {
    private final ConsoleManager.ConsoleAccessor consoleAccessor;
    private final WindowManager.WindowAccessor windowAccessor;
    private final ConsoleRenderer consoleRenderer;
    private final ConsoleManager consoleManager;
    private long lastUpdated = 0;

    @Override
    protected WindowRenderContext createContext() {
        return new WindowRenderContext();
    }

    @Override
    public void render(final WindowView window) {
        render(windowAccessor.get(window.getID()));
    }

    private void render(final WindowManager.WindowEntity window) {
        try (final var _ = consoleRenderer.withContext(getContext())) {
            if (window.getConsole() == null)
                window.setConsole(consoleAccessor.get(consoleManager.create(window.getName())));

            if (window.getConsole().isVisible())
                consoleRenderer.render(window.getConsole());
        }

        glfwSwapBuffers(window.getInternalID());

        final var now = System.currentTimeMillis();
        if (now - lastUpdated >= 1000) {
            lastUpdated = now;
            glfwSetWindowTitle(window.getInternalID(), window.getName() + ": " + getContext().getFrameRate());
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
