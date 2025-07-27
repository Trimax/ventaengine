package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.binders.*;
import com.venta.engine.exceptions.ObjectRenderingException;
import com.venta.engine.managers.MeshManager;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.model.view.MeshView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class MeshRenderer extends AbstractRenderer<MeshView, MeshRenderer.MeshRenderContext, SceneRenderer.SceneRenderContext> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final MeshManager.MeshAccessor meshAccessor;

    private final MaterialBinder materialBinder;
    private final ObjectBinder objectBinder;
    private final MatrixBinder matrixBinder;
    private final CameraBinder cameraBinder;
    private final LightBinder lightBinder;

    @Override
    protected MeshRenderContext createContext() {
        return new MeshRenderContext();
    }

    @Override
    public void render(final MeshView object) {
        if (!object.isVisible() || !object.hasProgram())
            return;

        render(meshAccessor.get(object.getID()), programAccessor.get(object.getProgram()));
    }

    private void render(final MeshManager.MeshEntity object, final ProgramManager.ProgramEntity program) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(program.getInternalID());
        glBindVertexArray(object.getVertexArrayObjectID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        cameraBinder.bind(program, getContext().getParent().getCamera());

        objectBinder.bind(program, object);
        matrixBinder.bind(program, context.getParent().getViewProjectionMatrixBuffer(), context.getModelMatrixBuffer(), context.getNormalMatrixBuffer());
        materialBinder.bind(program, object.getMaterial());

        lightBinder.bind(program, context.getScene().getAmbientLight());
        lightBinder.bind(program, context.getScene().getLights());

        if (object.getFacetsCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getFacetsBufferID());
            glDrawElements(GL_TRIANGLES, object.getFacetsCount(), GL_UNSIGNED_INT, 0);
        }

        if (object.getEdgesCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getEdgesBufferID());
            glDrawElements(GL_LINES, object.getEdgesCount(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    static final class MeshRenderContext extends AbstractRenderContext<SceneRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneView scene;

        public MeshRenderContext withModelMatrix(final Vector3f position, final Vector3f rotation, final Vector3f scale) {
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

        public MeshRenderContext withScene(final SceneView scene) {
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
