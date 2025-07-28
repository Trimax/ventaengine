package com.venta.engine.renderers;

import static org.lwjgl.glfw.GLFW.*;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowRenderer extends AbstractRenderer<WindowView, WindowRenderer.WindowRenderContext, WindowRenderer.WindowRenderContext> {
    private final WindowManager.WindowAccessor windowAccessor;
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
        glfwSwapBuffers(window.getInternalID());

        final var now = System.currentTimeMillis();
        if (now - lastUpdated >= 1000) {
            lastUpdated = now;
            glfwSetWindowTitle(window.getInternalID(), window.getName() + ": " + getContext().getFrameRate());
        }
    }

    @Getter(AccessLevel.PACKAGE)
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
