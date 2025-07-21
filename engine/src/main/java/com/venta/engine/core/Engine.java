package com.venta.engine.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import com.venta.engine.annotations.Component;
import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.renderers.SceneRenderer;
import com.venta.engine.renderers.WindowRenderer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Engine implements Runnable {
    private final WindowRenderer windowRenderer;
    private final SceneRenderer sceneRenderer;
    private final Context context;

    @Setter
    private Venta venta;

    public void initialize(final WindowConfiguration windowConfiguration) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        context.getWindowManager().setCurrent(context.getWindowManager()
                .create(windowConfiguration.title(), windowConfiguration.width(), windowConfiguration.height()));

        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));

        GL.createCapabilities();
    }

    @Override
    public void run() {
        glEnable(GL_DEPTH_TEST);

        final var window = context.getWindowManager().getCurrent();
        while (!windowRenderer.shouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            final var scene = context.getSceneManager().getCurrent();
            if (scene != null)
                sceneRenderer.render(scene);

            windowRenderer.render(window);
            glfwPollEvents();

            venta.onUpdate(windowRenderer.getDelta(), context);
        }

        context.cleanup();
        glfwTerminate();
    }
}

