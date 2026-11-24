package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.managers.implementation.EmitterManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.TerrainSurfaceManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WaterSurfaceManagerImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.renderers.entity.CubemapEntityRenderer;
import io.github.trimax.venta.engine.utils.DrawingOrder;
import lombok.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import static org.lwjgl.opengl.GL11C.*;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneInstanceRenderer
        extends AbstractInstanceRenderer<SceneInstanceImplementation, SceneInstanceRenderer.SceneRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final TerrainSurfaceManagerImplementation terrainSurfaceManager;
    private final WaterSurfaceManagerImplementation waterSurfaceManager;
    private final EmitterManagerImplementation emitterManager;
    private final ObjectManagerImplementation objectManager;
    private final TerrainSurfaceInstanceRenderer terrainSurfaceRenderer;
    private final WaterSurfaceInstanceRenderer waterSurfaceRenderer;
    private final BillboardInstanceRenderer billboardRenderer;
    private final EmitterInstanceRenderer emitterRenderer;
    private final ObjectInstanceRenderer objectRenderer;
    private final CubemapEntityRenderer cubemapRenderer;

    @Override
    public SceneRenderContext createContext() {
        return new SceneRenderContext();
    }

    @Override
    @SneakyThrows
    public void render(final SceneInstanceImplementation scene) {
        if (scene == null)
            return;

        final var cubemap = scene.getSkybox();
        if (cubemap != null)
            try (final var _ = cubemapRenderer.withContext(getContext())
                    .withScene(scene)) {
                cubemapRenderer.render(cubemap);
            }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (final var surface : scene.getTerrainSurfaces())
            try (final var _ = terrainSurfaceRenderer.withContext(getContext())
                    .withModelMatrix(surface.getTransform().getMatrix())
                    .withScene(scene)) {
                terrainSurfaceRenderer.render(terrainSurfaceManager.getInstance(surface.getID()));
            }

        for (final var object : getContext().getObjectsInDrawingOrder(scene))
            try (final var _ = objectRenderer.withContext(getContext())
                    .withScene(scene)) {
                objectRenderer.render(objectManager.getInstance(object.getID()));
            }

        // Water is semi-transparent, so it is rendered after the geometry which can be seen through it
        for (final var surface : scene.getWaterSurfaces())
            try (final var _ = waterSurfaceRenderer.withContext(getContext())
                    .withModelMatrix(surface.getTransform().getMatrix())
                    .withScene(scene)) {
                waterSurfaceRenderer.render(waterSurfaceManager.getInstance(surface.getID()));
            }
            
        glDepthMask(false);
        for (final var billboard : scene.getBillboards())
            try (final var _ = billboardRenderer.withContext(getContext())
                    .withModelMatrix(billboard.getTransform().getMatrix())
                    .withScene(scene)) {
                billboardRenderer.render(billboard);
            }

        for (final var emitter : scene.getEmitters())
            try (final var _ = emitterRenderer.withContext(getContext())
                    .withScene(scene)) {
                emitterRenderer.render(emitterManager.getInstance(emitter.getID()));
            }
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext<SceneRenderContext> {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer projectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer viewMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();
        private final Matrix4f projectionMatrix = new Matrix4f();
        private final Matrix4f viewMatrix = new Matrix4f();

        /** The order of the objects of a frame: its lists and arrays are kept, so a frame allocates nothing for it */
        private final DrawingOrder<ObjectInstanceImplementation> drawingOrder = new DrawingOrder<>();
        private final ToDoubleFunction<ObjectInstanceImplementation> distanceToCamera = this::getDistanceToCamera;
        private final Vector3f objectPosition = new Vector3f();

        private CameraInstanceImplementation camera;
        private Engine.VentaTime time;

        /** Programs are cached by the registry, so they are compared by reference */
        @Getter(AccessLevel.NONE)
        private final Set<ProgramEntityImplementation> programsWithLights = Collections.newSetFromMap(new IdentityHashMap<>());

        /** True for the first call per program in a frame: lights are uniforms of the program, so they are uploaded once */
        public boolean markLightsBound(@NonNull final ProgramEntityImplementation program) {
            return programsWithLights.add(program);
        }

        /** Cutout objects write depth and go first, the objects that mix colors go from the farthest to the nearest */
        public List<ObjectInstanceImplementation> getObjectsInDrawingOrder(final SceneInstanceImplementation scene) {
            return drawingOrder.order(scene.getObjects(), ObjectInstanceImplementation::hasCutout, distanceToCamera);
        }

        private double getDistanceToCamera(final ObjectInstanceImplementation object) {
            return object.getTransform().getMatrix().getTranslation(objectPosition).distanceSquared(camera.getPosition());
        }

        public SceneRenderContext with(final WindowState window, final CameraInstanceImplementation camera) {
            window.getProjectionMatrix().mul(camera.getViewMatrix(), viewProjectionMatrix);
            projectionMatrix.set(window.getProjectionMatrix());
            viewMatrix.set(camera.getViewMatrix());

            viewProjectionMatrix.get(viewProjectionMatrixBuffer);
            projectionMatrix.get(projectionMatrixBuffer);
            viewMatrix.get(viewMatrixBuffer);
            this.camera = camera;

            return this;
        }

        public SceneRenderContext withTime(final Engine.VentaTime time) {
            this.time = time;
            return this;
        }

        @Override
        public void close() {
            viewProjectionMatrixBuffer.clear();
            projectionMatrixBuffer.clear();
            viewMatrixBuffer.clear();
            programsWithLights.clear();
            time = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }
}
