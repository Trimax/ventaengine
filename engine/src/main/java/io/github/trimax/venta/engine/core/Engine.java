package io.github.trimax.venta.engine.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.managers.WindowManager;
import io.github.trimax.venta.engine.renderers.DebugRenderer;
import io.github.trimax.venta.engine.renderers.SceneRenderer;
import io.github.trimax.venta.engine.renderers.WindowRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Engine implements Runnable {
    private final WindowManager.WindowAccessor windowAccessor;
    private final CameraManager.CameraAccessor cameraAccessor;
    private final WindowRenderer windowRenderer;
    private final SceneRenderer sceneRenderer;
    private final DebugRenderer debugRenderer;
    private final VentaContext context;

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
        context.setRenderConfiguration(configuration.getRenderConfiguration());
        context.getWindowManager().setCurrent(context.getWindowManager().create(configuration.getWindowConfiguration(), ventaEngineApplication.getInputHandler()));

        GL.createCapabilities();
        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));
    }

    @Override
    public void run() {
        glEnable(GL_DEPTH_TEST);

        final var renderConfiguration = application.getConfiguration().getRenderConfiguration();
        final var updateHandler = application.getUpdateHandler();
        final var fpsCounter = new FPSCounter();
        final var time = new VentaTime();

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

            if (renderConfiguration.isDebugEnabled())
                try (final var _ = debugRenderer.withContext(null)
                        .with(window, camera)) {
                    debugRenderer.render(context.getSceneManager().getCurrent());
                }

            try (final var _ = windowRenderer.withContext(null)
                    .withFrameRate((int) fpsCounter.getCurrentFps())) {
                windowRenderer.render(window);
            }

            glfwPollEvents();

            time.setDelta(fpsCounter.tick());
            updateHandler.onUpdate(time, context);
        }

        context.cleanup();
        glfwTerminate();
    }

    @Getter
    public static final class VentaTime {
        private double timeElapsed = 0.0;
        private double delta;

        private void setDelta(final double delta) {
            this.timeElapsed += delta;
            this.delta = delta;
        }
    }
}

