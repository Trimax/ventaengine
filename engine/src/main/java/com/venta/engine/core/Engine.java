package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.interfaces.VentaEngineApplication;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.renderers.SceneRenderer;
import com.venta.engine.renderers.WindowRenderer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Engine implements Runnable {
    private final WindowManager.WindowAccessor windowAccessor;
    private final CameraManager.CameraAccessor cameraAccessor;
    private final WindowRenderer windowRenderer;
    private final SceneRenderer sceneRenderer;
    private final Context context;

    private VentaEngineApplication application;

    public void initialize(@NonNull final VentaEngineApplication ventaEngineApplication) {
        this.application = ventaEngineApplication;
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        final var configuration = ventaEngineApplication.getConfiguration();
        context.getWindowManager().setCurrent(context.getWindowManager().create(configuration.getWindowConfiguration(), ventaEngineApplication.getInputHandler()));
        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));

        GL.createCapabilities();

        final var origin = context.getObjectManager().load("origin");

        origin.setScale(new Vector3f(100000f));
        origin.setDrawMode(DrawMode.Edge);
        origin.setVisible(configuration.getRenderConfiguration().isOriginVisible());
        context.getSceneManager().getCurrent().add(origin);
    }

    @Override
    public void run() {
        glEnable(GL_DEPTH_TEST);

        final var fpsCounter = new FPSCounter();

        boolean windowClosed = false;
        while (!windowClosed) {
            final var window = windowAccessor.get(context.getWindowManager().getCurrent());
            windowClosed = glfwWindowShouldClose(window.getInternalID());

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            final var camera = cameraAccessor.get(context.getCameraManager().getCurrent());
            try (final var _ = sceneRenderer.withContext(null)
                    .with(window, camera)) {
                sceneRenderer.render(context.getSceneManager().getCurrent());
            }

            try (final var _ = windowRenderer.withContext(null)
                    .withFrameRate((int) fpsCounter.getCurrentFps())) {
                windowRenderer.render(window);
            }

            glfwPollEvents();

            application.onUpdate(fpsCounter.tick(), context);
        }

        context.cleanup();
        glfwTerminate();
    }
}

