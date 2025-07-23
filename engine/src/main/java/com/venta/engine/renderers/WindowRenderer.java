package com.venta.engine.renderers;

import static org.lwjgl.glfw.GLFW.*;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.Getter;

@Component
public final class WindowRenderer extends AbstractRenderer<WindowManager.WindowEntity, WindowView, WindowRenderer.WindowRenderContext> {
    private long lastTime = System.nanoTime();
    private int frames = 0;
    private double fpsTimer = 0.0;
    private double lastDelta;

    @Override
    protected WindowRenderContext createContext() {
        return new WindowRenderContext();
    }

    @Override
    public void render(final WindowView window) {
        glfwSwapBuffers(window.entity.getId());

        final long now = System.nanoTime();
        lastDelta = (now - lastTime) / 1_000_000_000.0;

        lastTime = now;
        fpsTimer += lastDelta;
        frames++;

        if (fpsTimer >= 1.0) {
            final double fps = frames / fpsTimer;
            fpsTimer = 0.0;
            frames = 0;

            glfwSetWindowTitle(window.entity.getId(), window.entity.getTitle() + ": " + (int) fps);
        }
    }

    public double getDelta() {
        return lastDelta;
    }

    public boolean shouldClose(final WindowView window) {
        return glfwWindowShouldClose(window.entity.getId());
    }

    @Getter(AccessLevel.PACKAGE)
    public static final class WindowRenderContext extends AbstractRenderContext {
        @Override
        public void close() {
        }

        @Override
        public void destroy() {

        }
    }
}
