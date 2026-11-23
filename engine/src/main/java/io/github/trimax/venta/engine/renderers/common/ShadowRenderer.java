package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.binders.ElevationBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.memory.MatrixCache;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.shadow.ShadowCascade;
import io.github.trimax.venta.engine.model.common.shadow.ShadowFrame;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.utils.ShadowCascadeUtil;
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
    private final MatrixBinder matrixBinder;
    private final MatrixCache matrixCache;
    private final Memory memory;

    private final Vector3f cameraForward = new Vector3f();

    private ShadowMap map;
    private FloatBuffer matrixBuffer;

    /** The shadows of the last frame, null when the scene has none */
    @Getter
    private ShadowFrame frame;

    /** Draws the shadow maps of the frame when the sun of the scene casts shadows */
    public void render(final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera, @NonNull final WindowState window) {
        frame = null;

        final var light = scene == null ? null : scene.getDirectionalLight();
        if (light == null || !light.isCastShadows())
            return;

        if (map == null) {
            map = new ShadowMap(memory, DefinitionsCommon.SHADOW_RESOLUTION, DefinitionsCommon.MAX_CASCADES);
            matrixBuffer = MemoryUtil.memAllocFloat(16);
        }

        final var view = camera.getViewMatrix();
        final var cascades = ShadowCascadeUtil.getCascades(view, window.getProjectionMatrix(), light.getDirection(), light.getShadowDistance(),
                DefinitionsCommon.MAX_CASCADES, DefinitionsCommon.SHADOW_RESOLUTION);

        final var objects = programRegistry.get(ProgramType.Shadow.getProgramName());
        final var terrain = programRegistry.get(ProgramType.ShadowTerrain.getProgramName());

        // What is nearer to the sun than the cascade is clamped to its near plane and still casts its shadow
        glEnable(GL_DEPTH_CLAMP);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        for (int i = 0; i < cascades.size(); i++) {
            map.bindLayer(i);
            glClear(GL_DEPTH_BUFFER_BIT);

            drawTerrain(scene, terrain, cascades.get(i));
            drawObjects(scene, objects, cascades.get(i));
        }

        glUseProgram(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_DEPTH_CLAMP);

        glActiveTexture(TextureUnit.Shadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, map.getTextureID());
        glActiveTexture(GL_TEXTURE0);

        cameraForward.set(-view.m02(), -view.m12(), -view.m22()).normalize();
        frame = new ShadowFrame(cascades, light.getShadowDistance(), new Vector3f(cameraForward));
    }

    /** Releases the shadow map and the buffer, the next frame with shadows creates them again */
    public void release() {
        frame = null;

        if (map != null) {
            map.release();
            map = null;
        }

        if (matrixBuffer != null) {
            MemoryUtil.memFree(matrixBuffer);
            matrixBuffer = null;
        }
    }

    private void drawTerrain(final SceneInstanceImplementation scene, final ProgramEntityImplementation program, final ShadowCascade cascade) {
        glUseProgram(program.getInternalID());
        bindLight(program, cascade);

        for (final var surface : scene.getTerrainSurfaces())
            if (surface.isVisible())
                draw(program, surface);
    }

    private void drawObjects(final SceneInstanceImplementation scene, final ProgramEntityImplementation program, final ShadowCascade cascade) {
        glUseProgram(program.getInternalID());
        bindLight(program, cascade);

        for (final var object : scene.getObjects())
            if (object.isVisible())
                draw(program, object.getMesh(), object.getTransform().getMatrix(), 0);
    }

    private void draw(final ProgramEntityImplementation program, final TerrainSurfaceInstanceImplementation surface) {
        elevationBinder.bind(program, surface.getHeightmap(), surface.getElevations(), surface.getFactor(), surface.getGridMesh().getSize());
        bindModel(program, surface.getTransform().getMatrix());

        geometryHelper.render(surface.getGridMesh().getGeometry());
    }

    private void draw(final ProgramEntityImplementation program, final Node<MeshReference> node, final Matrix4f parentMatrix, final int depth) {
        if (node == null)
            return;

        final var matrix = matrixCache.get(depth).set(parentMatrix);
        if (node.hasValue()) {
            final var reference = node.value();
            if (reference.hasTransform())
                matrix.mul(reference.transform().getMatrix());

            if (reference.hasMesh()) {
                bindModel(program, matrix);
                geometryHelper.render(reference.mesh().getGeometry());
            }
        }

        if (node.hasChildren())
            for (final var child : node.children())
                draw(program, child, matrix, depth + 1);
    }

    private void bindLight(final ProgramEntityImplementation program, final ShadowCascade cascade) {
        cascade.matrix().get(matrixBuffer);
        matrixBinder.bindViewProjectionMatrix(program, matrixBuffer);
    }

    private void bindModel(final ProgramEntityImplementation program, final Matrix4f matrix) {
        matrix.get(matrixBuffer);
        matrixBinder.bindModelMatrix(program, matrixBuffer);
    }
}
