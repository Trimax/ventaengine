package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL20C.glUseProgram;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.binders.ElevationBinder;
import io.github.trimax.venta.engine.helpers.TerrainChunkHelper;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.WindBinder;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.helpers.FrustumHelper;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.memory.InstanceBuffer;
import io.github.trimax.venta.engine.memory.MatrixCache;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.draw.InstanceBatcher;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Draws what casts shadows into the shadow map that is bound: the terrain and the visible objects, as a light sees them
 * through a matrix, only the depth is kept. It is what the sun and the point lights have in common, they differ in the
 * matrices and in the state of the pass.
 *
 * <p>The copies of the meshes of the objects that reach into the view of the light are collected and drawn with instancing:
 * the meshes with a cutout surface need their own program, so they are collected apart. The batches are made for every pass
 * and kept between them, so a pass allocates nothing once they have grown to the size of the scene.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowCasterRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final TerrainChunkHelper terrainChunkHelper;
    private final ElevationBinder elevationBinder;
    private final GeometryHelper geometryHelper;
    private final MaterialBinder materialBinder;
    private final MatrixBinder matrixBinder;
    private final WindBinder windBinder;
    private final MatrixCache matrixCache;
    private final Memory memory;

    private final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> solids = new InstanceBatcher<>();
    private final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> cutouts = new InstanceBatcher<>();
    private final InstanceBuffer instanceBuffer = new InstanceBuffer();
    private final FrustumHelper culler = new FrustumHelper();

    private FloatBuffer matrixBuffer;

    /**
     * Draws the shadow casters of a scene into the bound framebuffer.
     *
     * @param matrix the view and the projection of the light: world space to the clip space of the map
     * @param planes the planes of the view that cull the objects, see {@link FrustumHelper}
     */
    public void draw(@NonNull final SceneInstanceImplementation scene, @NonNull final Matrix4fc matrix, final int planes, final Engine.VentaTime time) {
        if (matrixBuffer == null)
            matrixBuffer = MemoryUtil.memAllocFloat(16);

        final var objects = programRegistry.get(ProgramType.ShadowInstanced.getProgramName());
        final var cutoutObjects = programRegistry.get(ProgramType.ShadowCutoutInstanced.getProgramName());
        final var terrain = programRegistry.get(ProgramType.ShadowTerrain.getProgramName());

        culler.set(matrix, planes);
        drawTerrain(scene, terrain, matrix);

        collectObjects(scene, objects, cutoutObjects, matrix, planes);
        drawObjects(objects, cutoutObjects, matrix, scene, time);

        solids.clear();
        cutouts.clear();
        glUseProgram(0);
    }

    /** Releases the buffers, the next pass creates them again */
    public void release() {
        if (matrixBuffer != null) {
            MemoryUtil.memFree(matrixBuffer);
            matrixBuffer = null;
        }

        instanceBuffer.release(memory);
    }

    private void drawTerrain(final SceneInstanceImplementation scene, final ProgramEntityImplementation program, final Matrix4fc matrix) {
        glUseProgram(program.getInternalID());
        bindLight(program, matrix);

        for (final var surface : scene.getTerrainSurfaces())
            if (surface.isVisible())
                drawSurface(program, surface);
    }

    /** Collects the copies of the meshes of the visible objects that reach into the view of the light, and uploads them */
    private void collectObjects(final SceneInstanceImplementation scene, final ProgramEntityImplementation objects,
                                final ProgramEntityImplementation cutoutObjects, final Matrix4fc matrix, final int planes) {
        culler.set(matrix, planes);
        final var margin = scene.getWind() == null ? 0.f : scene.getWind().getStrength();

        for (final var object : scene.getObjects())
            if (object.isVisible() && culler.isVisible(object.getBox(), object.getTransform().getMatrix(), margin))
                collect(objects, cutoutObjects, object.getMaterial(), object.getMesh(object.getModelLod()), object.getTransform().getMatrix(), 0);

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

    /** Draws the collected copies as the light sees them with its matrix */
    private void drawObjects(final ProgramEntityImplementation objects, final ProgramEntityImplementation cutoutObjects, final Matrix4fc matrix,
                             final SceneInstanceImplementation scene, final Engine.VentaTime time) {
        drawBatches(solids, objects, matrix, 0, scene, time);
        drawBatches(cutouts, cutoutObjects, matrix, solids.getCopiesCount(), scene, time);
    }

    /**
     * Draws the batches with a program, the first of them starts at {@code first} in the buffer
     */
    private void drawBatches(final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> batcher,
                      final ProgramEntityImplementation program, final Matrix4fc matrix, final int first,
                      final SceneInstanceImplementation scene, final Engine.VentaTime time) {
        if (batcher.getBatches().isEmpty())
            return;

        glUseProgram(program.getInternalID());
        bindLight(program, matrix);
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

    private void drawSurface(final ProgramEntityImplementation program, final TerrainSurfaceInstanceImplementation surface) {
        elevationBinder.bind(program, surface.getHeightmap(), surface.getElevations(), surface.getFactor(), surface.getGridMesh().getSize());
        bindModel(program, surface.getTransform().getMatrix());

        elevationBinder.bindSkirt(program, surface.getChunks().getSkirtDepth(surface.getFactor()));
        terrainChunkHelper.draw(surface.getChunks(), surface.getTransform().getMatrix(), surface.getFactor(), culler);
    }

    private void bindLight(final ProgramEntityImplementation program, final Matrix4fc matrix) {
        matrix.get(matrixBuffer);
        matrixBinder.bindViewProjectionMatrix(program, matrixBuffer);
    }

    private void bindModel(final ProgramEntityImplementation program, final Matrix4f matrix) {
        matrix.get(matrixBuffer);
        matrixBinder.bindModelMatrix(program, matrixBuffer);
    }
}
