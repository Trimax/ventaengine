package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.binders.ElevationBinder;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.WindBinder;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.memory.InstanceBuffer;
import io.github.trimax.venta.engine.memory.MatrixCache;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.shadow.ShadowCascade;
import io.github.trimax.venta.engine.model.common.shadow.ShadowFrame;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.utils.FrustumCuller;
import io.github.trimax.venta.engine.utils.InstanceBatcher;
import io.github.trimax.venta.engine.utils.ShadowCascadeCalculator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Draws the shadow maps of the sun before the scene: the objects and the terrain are drawn into the cascades as the
 * sun sees them, only the depth is kept. The shaders of the scene take the maps from {@link #getFrame()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final ElevationBinder elevationBinder;
    private final GeometryHelper geometryHelper;
    private final MaterialBinder materialBinder;
    private final MatrixBinder matrixBinder;
    private final WindBinder windBinder;
    private final MatrixCache matrixCache;
    private final Memory memory;

    /**
     * What a frame needs is made once and reused: the cascades, their computation and the copies of the meshes of the
     * objects. The copies are collected for every cascade (only what reaches into the box of the cascade is) and drawn into
     * it: the meshes with a cutout surface need their own program, so they are collected apart.
     */
    private final ShadowCascadeCalculator calculator = new ShadowCascadeCalculator();
    private final Vector3f cameraForward = new Vector3f();
    private final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> solids = new InstanceBatcher<>();
    private final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> cutouts = new InstanceBatcher<>();
    private final InstanceBuffer instanceBuffer = new InstanceBuffer();
    private final FrustumCuller culler = new FrustumCuller();

    private ShadowMap map;
    private FloatBuffer matrixBuffer;

    /** The shadows of the last frame, it is not active when the scene has none */
    @Getter
    private final ShadowFrame frame = new ShadowFrame(DefinitionsCommon.MAX_CASCADES);

    /** Draws the shadow maps of the frame when the sun of the scene casts shadows */
    public void render(final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera, @NonNull final WindowState window,
                       final Engine.VentaTime time) {
        frame.deactivate();

        final var light = scene == null ? null : scene.getDirectionalLight();
        if (light == null || !light.isCastShadows())
            return;

        if (map == null) {
            map = new ShadowMap(memory, DefinitionsCommon.SHADOW_RESOLUTION, DefinitionsCommon.MAX_CASCADES);
            matrixBuffer = MemoryUtil.memAllocFloat(16);
        }

        final var view = camera.getViewMatrix();
        calculator.calculate(frame, view, window.getProjectionMatrix(), light.getDirection(), light.getShadowDistance(),
                DefinitionsCommon.MAX_CASCADES, DefinitionsCommon.SHADOW_RESOLUTION);

        final var objects = programRegistry.get(ProgramType.ShadowInstanced.getProgramName());
        final var cutoutObjects = programRegistry.get(ProgramType.ShadowCutoutInstanced.getProgramName());
        final var terrain = programRegistry.get(ProgramType.ShadowTerrain.getProgramName());

        // What is nearer to the sun than the cascade is clamped to its near plane and still casts its shadow
        glEnable(GL_DEPTH_CLAMP);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        for (int i = 0; i < frame.getCount(); i++) {
            map.bindLayer(i);
            glClear(GL_DEPTH_BUFFER_BIT);

            drawTerrain(scene, terrain, frame.getCascade(i));

            // A cascade holds what the sun sees in its box: the objects beside the box can't shadow anything in it
            collectObjects(scene, objects, cutoutObjects, frame.getCascade(i));
            drawObjects(objects, cutoutObjects, frame.getCascade(i), scene, time);

            solids.clear();
            cutouts.clear();
        }

        glUseProgram(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_DEPTH_CLAMP);

        glActiveTexture(TextureUnit.Shadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, map.getTextureID());
        glActiveTexture(GL_TEXTURE0);

        cameraForward.set(-view.m02(), -view.m12(), -view.m22()).normalize();
        frame.activate(cameraForward);
    }

    /** Releases the shadow map and the buffer, the next frame with shadows creates them again */
    public void release() {
        frame.deactivate();

        if (map != null) {
            map.release();
            map = null;
        }

        if (matrixBuffer != null) {
            MemoryUtil.memFree(matrixBuffer);
            matrixBuffer = null;
        }

        instanceBuffer.release(memory);
    }

    private void drawTerrain(final SceneInstanceImplementation scene, final ProgramEntityImplementation program, final ShadowCascade cascade) {
        glUseProgram(program.getInternalID());
        bindLight(program, cascade);

        for (final var surface : scene.getTerrainSurfaces())
            if (surface.isVisible())
                draw(program, surface);
    }

    /** Collects the copies of the meshes of the visible objects that reach into the box of the cascade, and uploads them */
    private void collectObjects(final SceneInstanceImplementation scene, final ProgramEntityImplementation objects,
                                final ProgramEntityImplementation cutoutObjects, final ShadowCascade cascade) {
        // Only the sides of the box cull: what is nearer to the sun than the map still casts its shadow into it
        culler.set(cascade.getMatrix(), FrustumCuller.SIDE_PLANES);
        final var margin = scene.getWind() == null ? 0.f : scene.getWind().getStrength();

        for (final var object : scene.getObjects())
            if (object.isVisible() && culler.isVisible(object.getBox(), object.getTransform().getMatrix(), margin))
                collect(objects, cutoutObjects, object.getMaterial(), object.getMesh(), object.getTransform().getMatrix(), 0);

        instanceBuffer.begin();
        append(solids);
        append(cutouts);
        instanceBuffer.upload(memory);
    }

    private void append(final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> batcher) {
        for (final var batch : batcher.getBatches())
            instanceBuffer.append(batch.getData(), batch.getLength());
    }

    private void collect(final ProgramEntityImplementation objects, final ProgramEntityImplementation cutoutObjects,
                         final MaterialEntityImplementation material, final Node<MeshReference> node,
                         final Matrix4f parentMatrix, final int depth) {
        if (node == null)
            return;

        final var matrix = matrixCache.get(depth).set(parentMatrix);
        if (node.hasValue()) {
            final var reference = node.value();
            if (reference.hasTransform())
                matrix.mul(reference.transform().getMatrix());

            if (reference.hasMesh()) {
                // Only a cutout surface needs its alpha: the others cast the shadow of their whole shape, whatever the material
                final var surface = reference.hasMaterial() ? reference.material() : material;
                if (surface != null && surface.isCutout())
                    cutouts.add(reference.mesh(), cutoutObjects, surface, true, matrix);
                else
                    solids.add(reference.mesh(), objects, surface != null && surface.followsWind() ? surface : null, true, matrix);
            }
        }

        if (node.hasChildren())
            for (final var child : node.children())
                collect(objects, cutoutObjects, material, child, matrix, depth + 1);
    }

    /** Draws the collected copies as the sun sees them with the matrix of the cascade */
    private void drawObjects(final ProgramEntityImplementation objects, final ProgramEntityImplementation cutoutObjects, final ShadowCascade cascade,
                             final SceneInstanceImplementation scene, final Engine.VentaTime time) {
        draw(solids, objects, cascade, 0, scene, time);
        draw(cutouts, cutoutObjects, cascade, solids.getCopiesCount(), scene, time);
    }

    /** Draws the batches with a program, the first of them starts at {@code first} in the buffer */
    private void draw(final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> batcher,
                      final ProgramEntityImplementation program, final ShadowCascade cascade, final int first,
                      final SceneInstanceImplementation scene, final Engine.VentaTime time) {
        if (batcher.getBatches().isEmpty())
            return;

        glUseProgram(program.getInternalID());
        bindLight(program, cascade);
        windBinder.bind(program, scene.getWind(), time);

        var offset = first;
        for (final var batch : batcher.getBatches()) {
            if (batch.getMaterial() != null)
                materialBinder.bind(program, batch.getMaterial());

            // The shadow sways like the surface, the material is bound for it when it follows the wind
            windBinder.bind(program, batch.getMaterial());
            windBinder.bind(program, batch.getShape());

            geometryHelper.renderInstanced(batch.getShape().getGeometry(), instanceBuffer.getId(), offset, batch.getCount());
            offset += batch.getCount();
        }
    }

    private void draw(final ProgramEntityImplementation program, final TerrainSurfaceInstanceImplementation surface) {
        elevationBinder.bind(program, surface.getHeightmap(), surface.getElevations(), surface.getFactor(), surface.getGridMesh().getSize());
        bindModel(program, surface.getTransform().getMatrix());

        geometryHelper.render(surface.getGridMesh().getGeometry());
    }

    private void bindLight(final ProgramEntityImplementation program, final ShadowCascade cascade) {
        cascade.getMatrix().get(matrixBuffer);
        matrixBinder.bindViewProjectionMatrix(program, matrixBuffer);
    }

    private void bindModel(final ProgramEntityImplementation program, final Matrix4f matrix) {
        matrix.get(matrixBuffer);
        matrixBinder.bindModelMatrix(program, matrixBuffer);
    }
}
