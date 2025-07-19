package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.renderers.SceneRenderer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Engine implements Runnable {
    private final SceneRenderer sceneRenderer;
    private final FPSCounter fpsCounter;
    private final Context context;

    @Setter
    private Venta venta;

    @Setter
    private WindowManager.WindowEntity window;

    public void initialize(final WindowConfiguration windowConfiguration) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = context.getWindowManager()
                .create(windowConfiguration.title(), windowConfiguration.width(), windowConfiguration.height());

        GL.createCapabilities();
    }

    @Override
    public void run() {
        glEnable(GL_DEPTH_TEST);

        glfwMakeContextCurrent(window.getId());
        glfwSwapInterval(1); // vertical synchronization (setting to 0 produces 5000 FPS)

        while (!glfwWindowShouldClose(window.getId())) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            final var scene = context.getSceneManager().getCurrent();
            if (scene != null)
                sceneRenderer.render(scene);

            glfwSwapBuffers(window.getId());
            glfwPollEvents();

            venta.onUpdate(fpsCounter.getDelta(), context);
            fpsCounter.count(window);
        }

        glfwTerminate();
    }
}

