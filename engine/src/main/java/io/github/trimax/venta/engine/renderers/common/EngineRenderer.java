package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.core.FPSCounter;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.helpers.LodHelper;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.renderers.instance.SceneInstanceRenderer;
import io.github.trimax.venta.engine.renderers.state.WindowStateRenderer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineRenderer {
    private final EngineController engineController;
    private final WindowController windowController;

    private final GeometryHelper geometryHelper;
    private final CameraManagerImplementation cameraManager;
    private final SceneManagerImplementation sceneManager;

    private final PostProcessingRenderer postProcessingRenderer;
    private final EnvironmentRenderer environmentRenderer;
    private final ShadowRenderer shadowRenderer;
    private final WindowStateRenderer windowRenderer;
    private final SceneInstanceRenderer sceneRenderer;
    private final DebugRenderer debugRenderer;

    private final LodHelper lodHelper = new LodHelper();

    /** Releases the GL objects of the renderers, the context must be alive */
    public void release() {
        sceneRenderer.release();
        shadowRenderer.release();
        environmentRenderer.release();
        postProcessingRenderer.release();
    }

    public void render(final FPSCounter fpsCounter, final Engine.VentaTime time) {
        final var window = windowController.get();
        if (glfwWindowShouldClose(window.getInternalID()))
            engineController.get().setApplicationRunning(false);

        geometryHelper.startFrame();

        final var camera = cameraManager.getInstance(cameraManager.getCurrent().getID());

        // The light of the skybox is convolved when the skybox is new
        environmentRenderer.prepare(sceneManager.getCurrent().getSkybox());

        // The levels of detail are chosen once: the shadows and the scene draw the same ones
        lodHelper.update(sceneManager.getCurrent().getObjects(), camera.getPosition(), window.getProjectionMatrix().m11(), (float) time.getDelta());

        // The shadow maps are drawn first, the scene takes them
        shadowRenderer.render(sceneManager.getCurrent(), camera, window, time);

        // The scene and the debug overlays (they need the depth of the scene) are rendered in HDR
        postProcessingRenderer.begin(window);

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

        // The console and the text are drawn over the tone mapped image
        final var scene = sceneManager.getCurrent();
        postProcessingRenderer.end(scene.getExposure(), scene.getToneMapping(), scene.getBloom());

        try (final var _ = windowRenderer.withContext(null)
                        .withFrameRate((int) fpsCounter.getCurrentFps())) {
            windowRenderer.render(window);
        }

        glfwPollEvents();
    }
}
