package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.*;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshInstanceRenderer extends
        AbstractInstanceRenderer<WaterSurfaceInstanceImplementation, GridMeshInstanceRenderer.GridMeshRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final GeometryHelper geometryHelper;
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
    public void render(final WaterSurfaceInstanceImplementation gridMesh) {
        glUseProgram(gridMesh.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, gridMesh.getDrawMode().getMode());

        lightBinder.bind(gridMesh.getProgram(), getContext().getScene().getAmbientLight());
        lightBinder.bind(gridMesh.getProgram(), getContext().getScene().getLights());

        cameraBinder.bind(gridMesh.getProgram(), getContext().getParent().getCamera());

        matrixBinder.bindModelMatrix(gridMesh.getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindViewProjectionMatrix(gridMesh.getProgram(), getContext().getParent().getViewProjectionMatrixBuffer());

        timeBinder.bind(gridMesh.getProgram(), getContext().getParent().getTime());
        waveBinder.bind(gridMesh.getProgram(), gridMesh.getWaveAmplitude());
        waveBinder.bind(gridMesh.getProgram(), gridMesh.getWaves());

        materialBinder.bind(gridMesh.getProgram(), gridMesh.getMaterial());
        textureBinder.bind(gridMesh.getProgram(), getContext().getScene().getSkybox());

        geometryHelper.render(gridMesh.getGridMesh().getGeometry());

        glUseProgram(0);
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
