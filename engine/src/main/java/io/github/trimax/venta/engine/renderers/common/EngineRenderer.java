package io.github.trimax.venta.engine.renderers.common;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.core.FPSCounter;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.renderers.instance.SceneInstanceRenderer;
import io.github.trimax.venta.engine.renderers.state.WindowStateRenderer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11C.*;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineRenderer {
    private final EngineController engineController;
    private final WindowController windowController;

    private final CameraManagerImplementation cameraManager;
    private final SceneManagerImplementation sceneManager;

    private final WindowStateRenderer windowRenderer;
    private final SceneInstanceRenderer sceneRenderer;
    private final DebugRenderer debugRenderer;

    public void render(final FPSCounter fpsCounter, final Engine.VentaTime time) {
        final var window = windowController.get();
        if (glfwWindowShouldClose(window.getInternalID()))
            engineController.get().setApplicationRunning(false);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var camera = cameraManager.getInstance(cameraManager.getCurrent().getID());
        try (final var _ = sceneRenderer.withContext(null)
                .withTime(time)
                .with(window, camera)) {
            sceneRenderer.render(sceneManager.getCurrent());
        }

        if (engineController.get().isDebugEnabled())
            try (final var _ = debugRenderer.withContext(null)
                    .with(window, camera)) {
                debugRenderer.render(sceneManager.getCurrent());
            }

        try (final var _ = windowRenderer.withContext(null)
                        .withFrameRate((int) fpsCounter.getCurrentFps())) {
            windowRenderer.render(window);
        }

        glfwPollEvents();
    }
}
