package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.*;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

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
import io.github.trimax.venta.engine.model.common.shared.Wind;
import io.github.trimax.venta.engine.model.common.draw.DrawingOrder;
import io.github.trimax.venta.engine.helpers.FrustumHelper;
import io.github.trimax.venta.engine.model.common.reflection.ReflectionProbe;
import io.github.trimax.venta.engine.model.common.reflection.ReflectionProbes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import io.github.trimax.venta.engine.renderers.common.PostProcessingRenderer;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import one.util.streamex.StreamEx;
import lombok.Setter;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneInstanceRenderer
        extends AbstractInstanceRenderer<SceneInstanceImplementation, SceneInstanceRenderer.SceneRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final TerrainSurfaceManagerImplementation terrainSurfaceManager;
    private final WaterSurfaceManagerImplementation waterSurfaceManager;
    private final PostProcessingRenderer postProcessingRenderer;
    private final EmitterManagerImplementation emitterManager;
    private final ObjectManagerImplementation objectManager;
    private final TerrainSurfaceInstanceRenderer terrainSurfaceRenderer;
    private final WaterSurfaceInstanceRenderer waterSurfaceRenderer;
    private final BillboardInstanceRenderer billboardRenderer;
    private final EmitterInstanceRenderer emitterRenderer;
    private final ObjectInstanceRenderer objectRenderer;
    private final CubemapEntityRenderer cubemapRenderer;

    /** Releases the GL objects of the renderers of the scene, the context must be alive */
    public void release() {
        objectRenderer.release();
    }

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

        // What is hidden is not drawn, nor does it cast a shadow (the shadow pass skips it too)
        for (final var surface : scene.getTerrainSurfaces()) {
            if (!surface.isVisible())
                continue;

            try (final var _ = terrainSurfaceRenderer.withContext(getContext())
                    .withModelMatrix(surface.getTransform().getMatrix())
                    .withScene(scene)) {
                terrainSurfaceRenderer.render(terrainSurfaceManager.getInstance(surface.getID()));
            }
        }

        final var objects = getContext().getObjectsInDrawingOrder(scene);
        try (final var _ = objectRenderer.withContext(getContext())
                .withScene(scene)) {
            objectRenderer.renderBatched(objects);
        }

        for (final var object : objects) {
            if (!object.isVisible() || objectRenderer.isBatchable(object) || !getContext().isInView(object, scene.getWind()))
                continue;

            try (final var _ = objectRenderer.withContext(getContext())
                    .withScene(scene)) {
                objectRenderer.render(objectManager.getInstance(object.getID()));
            }
        }

        // Water is semi-transparent, so it is rendered after the geometry which can be seen through it: the geometry is
        // resolved for it to read (the depth of the water, the bottom seen through it)
        final var water = !getContext().isReflection() && StreamEx.of(scene.getWaterSurfaces()).anyMatch(WaterSurfaceInstanceImplementation::isVisible);
        if (water) {
            postProcessingRenderer.resolveScene();
            getContext().setSceneResolved(true);
        }

        for (final var surface : scene.getWaterSurfaces()) {
            if (!water || !surface.isVisible())
                continue;

            try (final var _ = waterSurfaceRenderer.withContext(getContext())
                    .withModelMatrix(surface.getTransform().getMatrix())
                    .withScene(scene)) {
                waterSurfaceRenderer.render(waterSurfaceManager.getInstance(surface.getID()));
            }
        }

        // The reflection is the geometry and the sky: the particles and the billboards are not worth a second pass
        if (getContext().isReflection()) {
            glDisable(GL_BLEND);
            return;
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
        private final Matrix4f viewProjectionInverseMatrix = new Matrix4f();
        private final FloatBuffer viewProjectionInverseMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f projectionMatrix = new Matrix4f();
        private final Matrix4f viewMatrix = new Matrix4f();

        /** The order of the objects of a frame: its lists and arrays are kept, so a frame allocates nothing for it */
        private final DrawingOrder<ObjectInstanceImplementation> drawingOrder = new DrawingOrder<>();
        private final ToDoubleFunction<ObjectInstanceImplementation> distanceToCamera = this::getDistanceToCamera;
        private final Vector3f objectPosition = new Vector3f();

        /** What the camera doesn't see is not drawn */
        private final FrustumHelper culler = new FrustumHelper();

        private CameraInstanceImplementation camera;
        private Engine.VentaTime time;

        /** Where the shaders see the scene from: the camera, or its mirror image when the reflection is drawn */
        private final Vector3f cameraPosition = new Vector3f();

        /** Whether this is a reflection (of the water, or a face of a reflection probe): it has no water, particles or billboards */
        private boolean reflection;

        /** Whether the framebuffer of this pass is multisampled: the holes of the cutout surfaces can be smoothed by alpha to coverage */
        private boolean multisampled;

        /** The probes that the objects reflect, null where the objects reflect the skybox only (the reflections themselves) */
        private ReflectionProbes probes;

        /** The object whose probe is drawn: it is not in its own reflection */
        private ObjectInstanceImplementation excluded;

        /**
         * Whether the opaque scene of this frame is resolved to textures (see {@link PostProcessingRenderer#resolveScene()}).
         */
        @Setter
        private boolean sceneResolved;

        /** Programs are cached by the registry, so they are compared by reference */
        @Getter(AccessLevel.NONE)
        private final Set<ProgramEntityImplementation> programsWithLights = Collections.newSetFromMap(new IdentityHashMap<>());

        /** True for the first call per program in a frame: lights are uniforms of the program, so they are uploaded once */
        public boolean markLightsBound(@NonNull final ProgramEntityImplementation program) {
            return programsWithLights.add(program);
        }

        /** Solid (opaque and cutout) objects write depth and go first, the objects that mix colors go from the farthest to the nearest */
        public List<ObjectInstanceImplementation> getObjectsInDrawingOrder(final SceneInstanceImplementation scene) {
            return drawingOrder.order(scene.getObjects(), ObjectInstanceImplementation::isSolid, distanceToCamera);
        }

        private double getDistanceToCamera(final ObjectInstanceImplementation object) {
            return object.getTransform().getMatrix().getTranslation(objectPosition).distanceSquared(cameraPosition);
        }

        /**
         * Whether an object is at least partly in the view of the camera. The wind sways a surface out of its box, so the box
         * is made as big as the wind can push the surface.
         */
        public boolean isInView(final ObjectInstanceImplementation object, final Wind wind) {
            return object != excluded && culler.isVisible(object.getBox(), object.getTransform().getMatrix(), wind == null ? 0.f : wind.getStrength());
        }

        public SceneRenderContext with(final WindowState window, final CameraInstanceImplementation camera) {
            reflection = false;
            cameraPosition.set(camera.getPosition());
            this.camera = camera;

            return apply(camera.getViewMatrix(), window.getProjectionMatrix());
        }

        /**
         * The scene as the mirror image of the camera sees it, for the reflection of the water: its view and projection (with the
         * plane of the water cutting what is under it), and the position it looks from. What is drawn in the reflection
         * is the geometry and the sky only.
         */
        public SceneRenderContext withReflection(final CameraInstanceImplementation camera, final Matrix4fc view, final Matrix4fc projection, final Vector3fc position) {
            reflection = true;
            cameraPosition.set(position);
            this.camera = camera;

            return apply(view, projection);
        }

        private SceneRenderContext apply(final Matrix4fc view, final Matrix4fc projection) {
            projection.mul(view, viewProjectionMatrix);
            projectionMatrix.set(projection);
            viewMatrix.set(view);

            culler.set(viewProjectionMatrix, FrustumHelper.ALL_PLANES);
            viewProjectionMatrix.get(viewProjectionMatrixBuffer);
            viewProjectionMatrix.invert(viewProjectionInverseMatrix).get(viewProjectionInverseMatrixBuffer);
            projectionMatrix.get(projectionMatrixBuffer);
            viewMatrix.get(viewMatrixBuffer);

            return this;
        }

        /** The framebuffer of the pass has samples: the main pass draws into the multisampled one, the reflections don't */
        public SceneRenderContext withMultisampling(final boolean multisampled) {
            this.multisampled = multisampled;
            return this;
        }

        /** The objects reflect the probes that are ready in this pass */
        public SceneRenderContext withProbes(final ReflectionProbes probes) {
            this.probes = probes;
            return this;
        }

        /** The object is left out of this pass: the pass is the cube map of its probe */
        public SceneRenderContext excluding(final ObjectInstanceImplementation object) {
            this.excluded = object;
            return this;
        }

        /** The probe that an object reflects in this pass: null when it reflects the skybox */
        public ReflectionProbe getProbe(final ObjectInstanceImplementation object) {
            return probes == null ? null : probes.getReady(object);
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
            sceneResolved = false;
            reflection = false;
            probes = null;
            multisampled = false;
            excluded = null;
            time = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
            MemoryUtil.memFree(viewProjectionInverseMatrixBuffer);
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }
}
