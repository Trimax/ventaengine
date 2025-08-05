package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaState;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.core.FPSCounter;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineRenderer {
    private final WindowController windowController;

    private final CameraManagerImplementation cameraManager;
    private final SceneManagerImplementation sceneManager;

    private final WindowRenderer windowRenderer;
    private final DebugRenderer debugRenderer;
    private final SceneRenderer sceneRenderer;

    public void render(final VentaState state, final FPSCounter fpsCounter) {
        final var window = windowController.get();
        if (glfwWindowShouldClose(window.getInternalID()))
            state.setApplicationRunning(false);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var camera = cameraManager.getEntity(cameraManager.getCurrent().getID());
        try (final var _ = sceneRenderer.withContext(null)
                .with(window, camera)) {
            sceneRenderer.render(sceneManager.getCurrent());
        }

        if (state.isDebugEnabled())
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
