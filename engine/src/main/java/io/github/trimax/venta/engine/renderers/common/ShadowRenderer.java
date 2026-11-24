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
import io.github.trimax.venta.engine.binders.MaterialBinder;
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
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
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
    private final MatrixCache matrixCache;
    private final Memory memory;

    /** What a frame needs is made once and reused: the cascades, their computation and the pass over the objects */
    private final ShadowCascadeCalculator calculator = new ShadowCascadeCalculator();
    private final Vector3f cameraForward = new Vector3f();
    private final ObjectPass pass = new ObjectPass();

    private ShadowMap map;
    private FloatBuffer matrixBuffer;

    /** The shadows of the last frame, it is not active when the scene has none */
    @Getter
    private final ShadowFrame frame = new ShadowFrame(DefinitionsCommon.MAX_CASCADES);

    /** Draws the shadow maps of the frame when the sun of the scene casts shadows */
    public void render(final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera, @NonNull final WindowState window) {
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

        final var objects = programRegistry.get(ProgramType.Shadow.getProgramName());
        final var cutouts = programRegistry.get(ProgramType.ShadowCutout.getProgramName());
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
            drawObjects(scene, pass.with(objects, cutouts, frame.getCascade(i)));
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
    }

    private void drawTerrain(final SceneInstanceImplementation scene, final ProgramEntityImplementation program, final ShadowCascade cascade) {
        glUseProgram(program.getInternalID());
        bindLight(program, cascade);

        for (final var surface : scene.getTerrainSurfaces())
            if (surface.isVisible())
                draw(program, surface);
    }

    private void drawObjects(final SceneInstanceImplementation scene, final ObjectPass pass) {
        pass.active = null;

        for (final var object : scene.getObjects())
            if (object.isVisible())
                draw(pass, object.getMaterial(), object.getMesh(), object.getTransform().getMatrix(), 0);
    }

    private void draw(final ProgramEntityImplementation program, final TerrainSurfaceInstanceImplementation surface) {
        elevationBinder.bind(program, surface.getHeightmap(), surface.getElevations(), surface.getFactor(), surface.getGridMesh().getSize());
        bindModel(program, surface.getTransform().getMatrix());

        geometryHelper.render(surface.getGridMesh().getGeometry());
    }

    private void draw(final ObjectPass pass, final MaterialEntityImplementation material, final Node<MeshReference> node,
                      final Matrix4f parentMatrix, final int depth) {
        if (node == null)
            return;

        final var matrix = matrixCache.get(depth).set(parentMatrix);
        if (node.hasValue()) {
            final var reference = node.value();
            if (reference.hasTransform())
                matrix.mul(reference.transform().getMatrix());

            if (reference.hasMesh()) {
                // Only a cutout surface needs its alpha: the others cast the shadow of their whole shape
                final var surface = reference.hasMaterial() ? reference.material() : material;
                final var cutout = surface != null && surface.isCutout();
                final var program = cutout ? pass.cutouts : pass.objects;

                use(pass, program);
                if (cutout)
                    materialBinder.bind(program, surface);

                bindModel(program, matrix);
                geometryHelper.render(reference.mesh().getGeometry());
            }
        }

        if (node.hasChildren())
            for (final var child : node.children())
                draw(pass, material, child, matrix, depth + 1);
    }

    /** Switches to the program with the matrix of the cascade, nothing is done when it is already in use */
    private void use(final ObjectPass pass, final ProgramEntityImplementation program) {
        if (pass.active == program)
            return;

        glUseProgram(program.getInternalID());
        bindLight(program, pass.cascade);
        pass.active = program;
    }

    /** The programs and the cascade of the pass over the objects, and the program that is in use */
    private static final class ObjectPass {
        private ProgramEntityImplementation objects;
        private ProgramEntityImplementation cutouts;
        private ShadowCascade cascade;
        private ProgramEntityImplementation active;

        /** Sets up the pass for a cascade, the same instance serves every cascade of every frame */
        private ObjectPass with(final ProgramEntityImplementation objects, final ProgramEntityImplementation cutouts, final ShadowCascade cascade) {
            this.objects = objects;
            this.cutouts = cutouts;
            this.cascade = cascade;
            this.active = null;

            return this;
        }
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
