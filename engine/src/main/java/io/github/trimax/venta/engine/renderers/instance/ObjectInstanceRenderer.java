package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import org.joml.Matrix4f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.ObjectBinder;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.renderers.entity.MeshEntityRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectInstanceRenderer extends AbstractInstanceRenderer<ObjectInstanceImplementation, ObjectInstanceRenderer.ObjectRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final MeshEntityRenderer meshRenderer;

    private final ObjectBinder objectBinder;
    private final MatrixBinder matrixBinder;
    private final CameraBinder cameraBinder;
    private final LightBinder lightBinder;
    private final FogBinder fogBinder;

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

        fogBinder.bind(object.getProgram(), context.getScene().getFog());

        render(object.getProgram(), object.getMesh(), object.getTransform().getMatrix());

        glUseProgram(0);
    }

    private void render(final ProgramEntityImplementation program, final Node<MeshReference> node, final Matrix4f parentMatrix) {
        if (node == null)
            return;

        //TODO: Do not create the matrix here. Reuse buffer
        final Matrix4f localMatrix = new Matrix4f(parentMatrix);
        if (node.hasValue()) {
            final var reference = node.value();
            if (reference.hasTransform())
                localMatrix.mul(node.value().transform().getMatrix());

            render(program, reference, localMatrix);
        }

        if (node.hasChildren())
            for (final Node<MeshReference> child : node.children())
                render(program, child, localMatrix);
    }

    private void render(final ProgramEntityImplementation program, final MeshReference reference, final Matrix4f modelMatrix) {
        if (reference.hasMesh())
            try (var _ = meshRenderer.withContext(getContext())
                    .withMaterial(reference.material())
                    .withModelMatrix(modelMatrix)
                    .withProgram(program)) {
                meshRenderer.render(reference.mesh());
            }
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ObjectRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private SceneInstance scene;

        public ObjectRenderContext withScene(final SceneInstance scene) {
            this.scene = scene;
            return this;
        }

        @Override
        public void close() {
            scene = null;
        }

        @Override
        public void destroy() {
        }
    }
}
