package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.core.FPSCounter;
import io.github.trimax.venta.engine.helpers.TerrainChunkHelper;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.interfaces.VentaEngineFrameListener;
import io.github.trimax.venta.engine.model.common.overlay.EditorOverlay;
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

    private final TerrainChunkHelper terrainChunkHelper;
    private final PostProcessingRenderer postProcessingRenderer;
    private final ReflectionRenderer reflectionRenderer;
    private final ReflectionProbeRenderer reflectionProbeRenderer;
    private final EditorOverlayRenderer editorOverlayRenderer;
    private VentaEngineFrameListener frameListener;
    private EditorOverlay overlay;
    private boolean eventsPolled = true;
    private final LightShaftRenderer lightShaftRenderer;
    private final AmbientOcclusionRenderer ambientOcclusionRenderer;
    private final EnvironmentRenderer environmentRenderer;
    private final ShadowRenderer shadowRenderer;
    private final ShadowCasterRenderer shadowCasterRenderer;
    private final PointShadowRenderer pointShadowRenderer;
    private final WindowStateRenderer windowRenderer;
    private final SceneInstanceRenderer sceneRenderer;
    private final DebugRenderer debugRenderer;

    private final LodHelper lodHelper = new LodHelper();

    /** Releases the GL objects of the renderers, the context must be alive */
    public void release() {
        sceneRenderer.release();
        lightShaftRenderer.release();
        ambientOcclusionRenderer.release();
        shadowRenderer.release();
        pointShadowRenderer.release();
        shadowCasterRenderer.release();
        environmentRenderer.release();
        terrainChunkHelper.release();
        reflectionRenderer.release();
        reflectionProbeRenderer.release();
        editorOverlayRenderer.release();
        postProcessingRenderer.release();
    }

    /**
     * Gets every finished frame (see {@link VentaEngineFrameListener}), null for none.
     */
    /**
     * Whether the events of the window are taken at the end of every frame (the default). The host of an embedded engine takes its own
     * events, and the hidden window of the engine has none.
     */
    public void setEventsPolled(final boolean eventsPolled) {
        this.eventsPolled = eventsPolled;
    }

    public void setOverlay(final EditorOverlay overlay) {
        this.overlay = overlay;
    }

    public void setFrameListener(final VentaEngineFrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public void render(final FPSCounter fpsCounter, final Engine.VentaTime time) {
        final var window = windowController.get();
        if (glfwWindowShouldClose(window.getInternalID()))
            engineController.get().setApplicationRunning(false);

        geometryHelper.startFrame();

        // The camera sees the scene as it says (the planes and the field of view), in every pass of the frame
        window.getView().set(sceneManager.getCurrent() == null ? null : sceneManager.getCurrent().getView());
        terrainChunkHelper.startFrame();

        final var camera = cameraManager.getInstance(cameraManager.getCurrent().getID());

        // The light of the skybox is convolved when the skybox is new
        environmentRenderer.prepare(sceneManager.getCurrent().getSkybox());

        // The levels of detail are chosen once: the shadows and the scene draw the same ones
        lodHelper.update(sceneManager.getCurrent().getObjects(), camera.getPosition(), window.getProjectionMatrix().m11(), (float) time.getDelta());

        // The chunks of the terrains get their levels of detail by the distance to the camera, the same ones in every pass
        for (final var surface : sceneManager.getCurrent().getTerrainSurfaces())
            if (surface instanceof TerrainSurfaceInstanceImplementation terrain && terrain.isVisible())
                terrainChunkHelper.update(terrain.getChunks(), terrain.getTransform().getMatrix(), camera.getPosition(), terrain.getFactor(), terrain.getLodDistance());

        // The shadow maps are drawn first, the scene takes them
        shadowRenderer.render(sceneManager.getCurrent(), camera, window, time);
        pointShadowRenderer.render(sceneManager.getCurrent(), camera, time);

        // The objects that reflect their surroundings have a face of their cube maps drawn
        reflectionProbeRenderer.render(sceneManager.getCurrent(), camera, window, time);

        // The water that mirrors the scene has it drawn as its mirror image sees it, before the scene itself
        if (reflectionRenderer.begin(sceneManager.getCurrent(), camera, window)) {
            try (final var _ = sceneRenderer.withContext(null)
                    .withTime(time)
                    .withReflection(camera, reflectionRenderer.getViewMatrix(), reflectionRenderer.getProjectionMatrix(), reflectionRenderer.getCameraPosition())) {
                sceneRenderer.render(sceneManager.getCurrent());
            }

            reflectionRenderer.end();
        }

        // The scene and the debug overlays (they need the depth of the scene) are rendered in HDR
        postProcessingRenderer.begin(window);

        try (final var _ = sceneRenderer.withContext(null)
                .withTime(time)
                .withProbes(reflectionProbeRenderer.getProbes())
                .withMultisampling(postProcessingRenderer.isMultisampled())
                .with(window, camera)) {
            sceneRenderer.render(sceneManager.getCurrent());
        }

        if (engineController.get().isDebugEnabled())
            try (final var _ = debugRenderer.withContext(null)
                    .with(window, camera)) {
                debugRenderer.render(sceneManager.getCurrent());
            }

        // The scene is resolved, the corners are darkened and the shafts of the sun are added (both need its depth), then it is tone mapped
        final var scene = sceneManager.getCurrent();
        final var occlusion = ambientOcclusionRenderer.isActive(scene);
        final var shafts = lightShaftRenderer.isActive(scene);
        postProcessingRenderer.resolve(occlusion || shafts || overlay != null);

        // The light that the fog scatters is in the air in front of the surfaces, so it is not darkened with them
        if (occlusion)
            ambientOcclusionRenderer.render(scene, camera, window, postProcessingRenderer);

        if (shafts)
            lightShaftRenderer.render(scene, camera, window, postProcessingRenderer);

        postProcessingRenderer.end(scene.getExposure(), scene.getToneMapping(), scene.getBloom(), scene.getAntialiasing());

        // An editor puts its grid, its marks and its frame over the finished image, before the host takes it
        if (overlay != null)
            editorOverlayRenderer.render(scene, camera, window, overlay, postProcessingRenderer.getResolvedDepthTextureID());

        // The host that embeds the engine takes the finished image before the overlays and the swap of the buffers
        if (frameListener != null)
            try (final var stack = stackPush()) {
                final var framebufferWidth = stack.mallocInt(1);
                final var framebufferHeight = stack.mallocInt(1);
                glfwGetFramebufferSize(window.getInternalID(), framebufferWidth, framebufferHeight);
                frameListener.onFrame(framebufferWidth.get(0), framebufferHeight.get(0));
            }

        // The console and the text are drawn over the tone mapped image
        try (final var _ = windowRenderer.withContext(null)
                        .withFrameRate((int) fpsCounter.getCurrentFps())) {
            windowRenderer.render(window);
        }

        // An engine that is a part of another application leaves the events to it: they are handled in the middle of its frame otherwise, where
        // a dialog can't be shown
        if (eventsPolled)
            glfwPollEvents();
    }
}
