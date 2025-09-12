package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import java.nio.FloatBuffer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.binders.TimeBinder;
import io.github.trimax.venta.engine.binders.WaveBinder;
import io.github.trimax.venta.engine.model.instance.implementation.GridMeshInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshInstanceRenderer extends
        AbstractInstanceRenderer<GridMeshInstanceImplementation, GridMeshInstanceRenderer.GridMeshRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final MaterialBinder materialBinder;
    private final TextureBinder textureBinder;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final LightBinder lightBinder;
    private final WaveBinder waveBinder;
    private final TimeBinder timeBinder;

    @Override
    protected GridMeshRenderContext createContext() {
        return new GridMeshRenderContext();
    }

    @Override
    public void render(final GridMeshInstanceImplementation mesh) {
        glUseProgram(mesh.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, mesh.getDrawMode().getMode());

        lightBinder.bind(mesh.getProgram(), getContext().getScene().getAmbientLight());
        lightBinder.bind(mesh.getProgram(), getContext().getScene().getLights());

        cameraBinder.bind(mesh.getProgram(), getContext().getParent().getCamera());

        matrixBinder.bindModelMatrix(mesh.getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindViewProjectionMatrix(mesh.getProgram(), getContext().getParent().getViewProjectionMatrixBuffer());

        timeBinder.bind(mesh.getProgram(), getContext().getParent().getTime());
        waveBinder.bind(mesh.getProgram(), mesh.getMesh().getWaves());

        materialBinder.bind(mesh.getProgram(), mesh.getMaterial());

        textureBinder.bind(mesh.getProgram(), getContext().getScene().getSkybox());

        renderMesh(mesh);

        glUseProgram(0);
    }

    private static void renderMesh(final GridMeshInstanceImplementation gridMesh) {
        glBindVertexArray(gridMesh.getMesh().getVertexArrayObjectID());

        if (gridMesh.getMesh().getFacetsCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gridMesh.getMesh().getFacetsBufferID());
            glDrawElements(GL_TRIANGLES, gridMesh.getMesh().getFacetsCount(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class GridMeshRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneInstanceImplementation scene;

        public GridMeshRenderContext withModelMatrix(final Matrix4f matrix) {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();

            modelMatrix.identity()
                    .set(matrix);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public GridMeshRenderContext withScene(final SceneInstanceImplementation scene) {
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
