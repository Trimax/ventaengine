package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import java.util.List;

import org.joml.Matrix4f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.EnvironmentBinder;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.binders.WindBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.ObjectBinder;
import io.github.trimax.venta.engine.binders.ShadowBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.memory.InstanceBuffer;
import io.github.trimax.venta.engine.memory.MatrixCache;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.renderers.entity.MeshEntityRenderer;
import io.github.trimax.venta.engine.utils.InstanceBatcher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectInstanceRenderer extends AbstractInstanceRenderer<ObjectInstanceImplementation, ObjectInstanceRenderer.ObjectRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final ProgramRegistryImplementation programRegistry;
    private final MeshEntityRenderer meshRenderer;
    private final GeometryHelper geometryHelper;
    private final MaterialBinder materialBinder;
    private final MatrixCache matrixCache;
    private final Memory memory;

    private final TextureBinder textureBinder;
    private final ObjectBinder objectBinder;
    private final MatrixBinder matrixBinder;
    private final ShadowBinder shadowBinder;
    private final EnvironmentBinder environmentBinder;
    private final CameraBinder cameraBinder;
    private final LightBinder lightBinder;
    private final FogBinder fogBinder;
    private final WindBinder windBinder;

    @Override
    protected ObjectRenderContext createContext() {
        return new ObjectRenderContext();
    }

    @Override
    public void render(final ObjectInstanceImplementation object) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        final var program = getProgram(object);
        glUseProgram(program.getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        cameraBinder.bind(program, getContext().getParent().getCamera());
        objectBinder.bind(program, object);
        matrixBinder.bindViewProjectionMatrix(program, context.getParent().getViewProjectionMatrixBuffer());

        if (getContext().getParent().markLightsBound(program)) {
            lightBinder.bind(program, context.getScene().getDirectionalLight());
            lightBinder.bind(program, context.getScene().getAmbientLight());
            lightBinder.bind(program, context.getScene().getLights());
            shadowBinder.bind(program);
            environmentBinder.bind(program);
        }

        textureBinder.bind(program, context.getScene().getSkybox());
        fogBinder.bind(program, context.getScene().getFog());
        windBinder.bind(program, context.getScene().getWind(), context.getParent().getTime());

        render(program, object.getMaterial(), object.getMesh(), object.getTransform().getMatrix(), 0);

        glUseProgram(0);
    }

    /**
     * Whether the object is drawn together with the copies of the same meshes by {@link #renderBatched}. Only the solid
     * objects (opaque and cutout surfaces, see {@link ObjectInstanceImplementation#isSolid()}) are: they are drawn before
     * the rest in any order, so their order doesn't matter, and the objects that mix colors must keep the order from the
     * farthest to the nearest.
     */
    public boolean isBatchable(final ObjectInstanceImplementation object) {
        return object.isSolid() && object.getDrawMode() == DrawMode.Polygon && object.getProgram() == getPrograms().plainProgram;
    }

    /**
     * Draws the batchable objects: the copies of a mesh that share a material are drawn with one call, so a forest of
     * a few hundred trees costs as many calls as it has different meshes and materials.
     *
     * @param objects the objects of the scene, the ones that are hidden or not batchable are skipped
     */
    public void renderBatched(final List<ObjectInstanceImplementation> objects) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        final var programs = getPrograms();
        for (final var object : objects)
            if (object.isVisible() && isBatchable(object))
                collect(object.hasCutout() ? programs.instancedCutoutProgram : programs.instancedProgram, object, object.getMaterial(), object.getMesh(), object.getTransform().getMatrix(), 0);

        try {
            glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Polygon.getMode());

            ProgramEntityImplementation active = null;
            for (final var batch : context.batcher.getBatches()) {
                if (batch.getProgram() != active) {
                    active = batch.getProgram();
                    bindFrame(active);
                }

                render(batch);
            }
        } finally {
            context.batcher.clear();
            glUseProgram(0);
        }
    }

    /** Collects the copies of the meshes of an object: the same walk over its hierarchy as {@link #render(ProgramEntityImplementation, MaterialEntityImplementation, Node, Matrix4f, int)} */
    private void collect(final ProgramEntityImplementation program,
                         final ObjectInstanceImplementation object,
                         final MaterialEntityImplementation material,
                         final Node<MeshReference> node,
                         final Matrix4f parentMatrix,
                         final int depth) {
        if (node == null)
            return;

        final Matrix4f localMatrix = matrixCache.get(depth).set(parentMatrix);
        if (node.hasValue()) {
            final var reference = node.value();
            if (reference.hasTransform())
                localMatrix.mul(reference.transform().getMatrix());

            if (reference.hasMesh())
                getContext().batcher.add(reference.mesh(), program, reference.hasMaterial() ? reference.material() : material,
                        object.isLit(), localMatrix);
        }

        if (node.hasChildren())
            for (final Node<MeshReference> child : node.children())
                collect(program, object, material, child, localMatrix, depth + 1);
    }

    /** What the program of the batches takes from the frame: the camera, the lights, the sky and the fog */
    private void bindFrame(final ProgramEntityImplementation program) {
        final var context = getContext();

        glUseProgram(program.getInternalID());
        cameraBinder.bind(program, context.getParent().getCamera());
        matrixBinder.bindViewProjectionMatrix(program, context.getParent().getViewProjectionMatrixBuffer());

        if (context.getParent().markLightsBound(program)) {
            lightBinder.bind(program, context.getScene().getDirectionalLight());
            lightBinder.bind(program, context.getScene().getAmbientLight());
            lightBinder.bind(program, context.getScene().getLights());
            shadowBinder.bind(program);
            environmentBinder.bind(program);
        }

        textureBinder.bind(program, context.getScene().getSkybox());
        fogBinder.bind(program, context.getScene().getFog());
        windBinder.bind(program, context.getScene().getWind(), context.getParent().getTime());
    }

    private void render(final InstanceBatcher.Batch<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> batch) {
        final var context = getContext();
        final var program = batch.getProgram();
        final var material = batch.getMaterial();

        objectBinder.bind(program, batch.isLit());
        materialBinder.bind(program, material);
        windBinder.bind(program, material);
        windBinder.bind(program, batch.getShape());

        context.instanceBuffer.begin();
        context.instanceBuffer.append(batch.getData(), batch.getLength());
        context.instanceBuffer.upload(memory);

        // A one sided material has no back faces: they are never seen, so they are not drawn
        final var oneSided = material != null && !material.isDoubleSided();
        if (oneSided)
            glEnable(GL_CULL_FACE);

        geometryHelper.renderInstanced(batch.getShape().getGeometry(), context.instanceBuffer.getId(), 0, batch.getCount());

        if (oneSided)
            glDisable(GL_CULL_FACE);
    }

    private void render(final ProgramEntityImplementation program,
                        final MaterialEntityImplementation material,
                        final Node<MeshReference> node,
                        final Matrix4f parentMatrix,
                        final int depth) {
        if (node == null)
            return;

        final Matrix4f localMatrix = matrixCache.get(depth).set(parentMatrix);
        if (node.hasValue()) {
            final var reference = node.value();
            if (reference.hasTransform())
                localMatrix.mul(node.value().transform().getMatrix());

            render(program, material, reference, localMatrix);
        }

        if (node.hasChildren())
            for (final Node<MeshReference> child : node.children())
                render(program, material, child, localMatrix, depth + 1);
    }

    private void render(final ProgramEntityImplementation program,
                        final MaterialEntityImplementation material,
                        final MeshReference reference,
                        final Matrix4f modelMatrix) {
        if (reference.hasMesh())
            try (var _ = meshRenderer.withContext(getContext())
                    .withMaterial(reference.hasMaterial() ? reference.material() : material)
                    .withModelMatrix(modelMatrix)
                    .withProgram(program)) {
                meshRenderer.render(reference.mesh());
            }
    }

    /**
     * The program of an object. The shader of the objects has a variant with the holes of cutout surfaces: it is used only
     * for the objects that have such a surface, because a shader that can discard fragments loses the early depth test.
     */
    private ProgramEntityImplementation getProgram(final ObjectInstanceImplementation object) {
        if (!object.hasCutout())
            return object.getProgram();

        final var programs = getPrograms();
        return object.getProgram() == programs.plainProgram ? programs.cutoutProgram : object.getProgram();
    }

    /** The shader of the objects and its variants, found when the first cutout object is asked about */
    private ObjectRenderContext getPrograms() {
        final var context = getContext();
        if (context.cutoutProgram == null) {
            context.plainProgram = programRegistry.get(ProgramType.Default.getProgramName());
            context.cutoutProgram = programRegistry.get(ProgramType.DefaultCutout.getProgramName());
            context.instancedCutoutProgram = programRegistry.get(ProgramType.DefaultCutoutInstanced.getProgramName());
            context.instancedProgram = programRegistry.get(ProgramType.DefaultInstanced.getProgramName());
        }

        return context;
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ObjectRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private SceneInstanceImplementation scene;

        /** The shader of the objects and its variants: with the holes, and with the holes for many copies of a mesh */
        private ProgramEntityImplementation plainProgram;
        private ProgramEntityImplementation cutoutProgram;
        private ProgramEntityImplementation instancedCutoutProgram;
        private ProgramEntityImplementation instancedProgram;

        /** The copies of the meshes of a frame, they are drawn in batches */
        private final InstanceBatcher<MeshEntityImplementation, ProgramEntityImplementation, MaterialEntityImplementation> batcher = new InstanceBatcher<>();

        /** The buffer the batches are uploaded to, one batch at a time */
        private final InstanceBuffer instanceBuffer = new InstanceBuffer();

        public ObjectRenderContext withScene(final SceneInstanceImplementation scene) {
            this.scene = scene;
            return this;
        }

        @Override
        public void close() {
            scene = null;
        }

        @Override
        public void destroy() {
            instanceBuffer.destroy();
        }
    }
}
