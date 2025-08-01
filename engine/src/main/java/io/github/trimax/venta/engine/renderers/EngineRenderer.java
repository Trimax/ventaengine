package io.github.trimax.venta.engine.renderers;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11C.*;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.FPSCounter;
import io.github.trimax.venta.engine.core.VentaState;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.managers.WindowManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineRenderer {
    private final WindowManager.WindowAccessor windowAccessor;
    private final CameraManager.CameraAccessor cameraAccessor;

    private final CameraManager cameraManager;
    private final WindowManager windowManager;
    private final SceneManager sceneManager;

    private final WindowRenderer windowRenderer;
    private final DebugRenderer debugRenderer;
    private final SceneRenderer sceneRenderer;

    public void render(final VentaState state, final FPSCounter fpsCounter) {
        final var window = windowAccessor.get(windowManager.getCurrent());
        if (glfwWindowShouldClose(window.getInternalID()))
            state.setApplicationRunning(false);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var camera = cameraAccessor.get(cameraManager.getCurrent());
        try (final var _ = sceneRenderer.withContext(null)
                .with(window, camera)) {
            sceneRenderer.render(sceneManager.getCurrent());
        }

        if (isDebugEnabled(state))
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

    private static boolean isDebugEnabled(final VentaState state) {
        return state.getApplication().getConfiguration().getRenderConfiguration().isDebugEnabled();
    }
}
