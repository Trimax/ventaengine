package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.WindowView;

import static org.lwjgl.glfw.GLFW.*;

@Component
public final class WindowRenderer implements AbstractRenderer<WindowManager.WindowEntity, WindowView> {
    private long lastTime = System.nanoTime();
    private int frames = 0;
    private double fpsTimer = 0.0;

    @Override
    public void render(final WindowView window) {
        glfwSwapBuffers(window.entity.getId());

        final long now = System.nanoTime();
        final double delta = (now - lastTime) / 1_000_000_000.0;

        lastTime = now;
        fpsTimer += delta;
        frames++;

        if (fpsTimer >= 1.0) {
            final double fps = frames / fpsTimer;
            fpsTimer = 0.0;
            frames = 0;

            glfwSetWindowTitle(window.entity.getId(), window.entity.getTitle() + ": " + (int) fps);
        }
    }

    public double getDelta() {
        return (System.nanoTime() - lastTime) / 1_000_000_000.0;
    }

    public boolean shouldClose(final WindowView window) {
        return glfwWindowShouldClose(window.entity.getId());
    }
}
