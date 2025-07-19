package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.WindowManager;

import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;

@Component
final class FPSCounter {
    private long lastTime = System.nanoTime();
    private int frames = 0;
    private double fpsTimer = 0.0;

    public void count(final WindowManager.WindowEntity window) {
        final long now = System.nanoTime();
        final double delta = (now - lastTime) / 1_000_000_000.0;

        lastTime = now;
        fpsTimer += delta;
        frames++;

        if (fpsTimer >= 1.0) {
            final double fps = frames / fpsTimer;
            fpsTimer = 0.0;
            frames = 0;

            glfwSetWindowTitle(window.getId(), window.getTitle() + ": " + (int) fps);
        }
    }

    public double getDelta() {
        return (System.nanoTime() - lastTime) / 1_000_000_000.0;
    }
}
