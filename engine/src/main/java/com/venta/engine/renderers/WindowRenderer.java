package com.venta.engine.renderers;

import static org.lwjgl.glfw.GLFW.*;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.Getter;

@Component
public final class WindowRenderer extends AbstractRenderer<WindowManager.WindowEntity, WindowView, WindowRenderer.WindowRenderContext, WindowRenderer.WindowRenderContext> {
    private long lastUpdated = 0;

    @Override
    protected WindowRenderContext createContext() {
        return new WindowRenderContext();
    }

    @Override
    public void render(final WindowView window) {
        glfwSwapBuffers(window.entity.getInternalID());

        final var now = System.currentTimeMillis();
        if (now - lastUpdated >= 1000) {
            lastUpdated = now;
            glfwSetWindowTitle(window.entity.getInternalID(), window.entity.getTitle() + ": " + getContext().getFrameRate());
        }
    }

    public boolean shouldClose(final WindowView window) {
        return glfwWindowShouldClose(window.entity.getInternalID());
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
