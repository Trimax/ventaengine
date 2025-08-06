package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.ObjectBinder;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.hierarchy.Node;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.renderers.entity.MeshEntityRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectInstanceRenderer extends AbstractInstanceRenderer<ObjectInstanceImplementation, ObjectInstanceRenderer.ObjectRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final MeshEntityRenderer meshRenderer;

    private final ObjectBinder objectBinder;
    private final MatrixBinder matrixBinder;
    private final CameraBinder cameraBinder;
    private final LightBinder lightBinder;

    @Override
    protected ObjectRenderContext createContext() {
        return new ObjectRenderContext();
    }

    @Override
    public void render(final ObjectInstanceImplementation object) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(object.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        cameraBinder.bind(object.getProgram(), getContext().getParent().getCamera());
        objectBinder.bind(object.getProgram(), object);
        matrixBinder.bindViewProjectionMatrix(object.getProgram(), context.getParent().getViewProjectionMatrixBuffer());

        lightBinder.bind(object.getProgram(), context.getScene().getAmbientLight());
        lightBinder.bind(object.getProgram(), context.getScene().getLights());

        render(object.getProgram(), object.getMesh(), object.getTransform().getMatrix());

        glUseProgram(0);
    }

    private void render(final ProgramEntityImplementation program, final Node<MeshReference> node, final Matrix4f parentMatrix) {
        if (node == null)
            return;

        //TODO: Do not create the matrix here. Reuse buffer
        final Matrix4f localMatrix = new Matrix4f(parentMatrix);
        if (node.value() != null)
            localMatrix.mul(node.value().transform().getMatrix());

        if (node.value() != null)
            render(program, node.value(), localMatrix);

        if (node.hasChildren())
            for (final Node<MeshReference> child : node.children())
                render(program, child, localMatrix);
    }

    private void render(final ProgramEntityImplementation program, final MeshReference reference, final Matrix4f modelMatrix) {
        getContext().withModelMatrix(modelMatrix);

        matrixBinder.bindModelMatrix(program, getContext().getModelMatrixBuffer());
        matrixBinder.bindNormalMatrix(program, getContext().getNormalMatrixBuffer());

        if (reference.hasMesh())
            try (var _ = meshRenderer.withContext(getContext())
                    .withMaterial(reference.material())
                    .withProgram(program)) {
                meshRenderer.render(reference.mesh());
            }
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ObjectRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneInstance scene;

        public ObjectRenderContext withModelMatrix(final Vector3f position, final Vector3f rotation, final Vector3f scale) {
            modelMatrix.identity()
                    .translate(position)
                    .rotateX(rotation.x)
                    .rotateY(rotation.y)
                    .rotateZ(rotation.z)
                    .scale(scale);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public ObjectRenderContext withModelMatrix(final Matrix4f matrix) {
            modelMatrix.identity()
                    .set(matrix);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public ObjectRenderContext withScene(final SceneInstance scene) {
            this.scene = scene;
            return this;
        }

        @Override
        public void close() {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();
            scene = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(normalMatrixBuffer);
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
