package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.binders.*;
import com.venta.engine.exceptions.ObjectRenderingException;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
final class ObjectRenderer extends AbstractRenderer<ObjectView, ObjectRenderer.ObjectRenderContext, SceneRenderer.SceneRenderContext> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ObjectManager.ObjectAccessor objectAccessor;
    private final MeshRenderer meshRenderer;

    private final MaterialBinder materialBinder;
    private final ObjectBinder objectBinder;
    private final MatrixBinder matrixBinder;
    private final CameraBinder cameraBinder;
    private final LightBinder lightBinder;

    @Override
    protected ObjectRenderContext createContext() {
        return new ObjectRenderContext();
    }

    @Override
    public void render(final ObjectView object) {
        if (!object.isVisible() || !object.hasProgram())
            return;

        render(objectAccessor.get(object.getID()), programAccessor.get(object.getProgram()));
    }

    private void render(final ObjectManager.ObjectEntity object, final ProgramManager.ProgramEntity program) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(program.getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        cameraBinder.bind(program, getContext().getParent().getCamera());
        objectBinder.bind(program, object);
        matrixBinder.bind(program, context.getParent().getViewProjectionMatrixBuffer(), context.getModelMatrixBuffer(), context.getNormalMatrixBuffer());

        lightBinder.bind(program, context.getScene().getAmbientLight());
        lightBinder.bind(program, context.getScene().getLights());

        try (var _ = meshRenderer.withContext(getContext())
                        .withProgram(program)) {
            meshRenderer.render(object.getMesh());
        }

        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    static final class ObjectRenderContext extends AbstractRenderContext<SceneRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneView scene;

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

        public ObjectRenderContext withScene(final SceneView scene) {
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
