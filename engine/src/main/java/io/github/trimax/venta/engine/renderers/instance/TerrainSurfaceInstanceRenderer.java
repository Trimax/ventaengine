package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.*;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
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
public final class TerrainSurfaceInstanceRenderer extends
        AbstractInstanceRenderer<TerrainSurfaceInstanceImplementation, TerrainSurfaceInstanceRenderer.TerrainSurfaceRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final GeometryHelper geometryHelper;
    private final ElevationBinder elevationBinder;
    private final MaterialBinder materialBinder;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final LightBinder lightBinder;
    private final TimeBinder timeBinder;

    @Override
    protected TerrainSurfaceRenderContext createContext() {
        return new TerrainSurfaceRenderContext();
    }

    @Override
    public void render(final TerrainSurfaceInstanceImplementation surface) {
        glUseProgram(surface.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, surface.getDrawMode().getMode());

        lightBinder.bind(surface.getProgram(), getContext().getScene().getDirectionalLight());
        lightBinder.bind(surface.getProgram(), getContext().getScene().getAmbientLight());
        lightBinder.bind(surface.getProgram(), getContext().getScene().getLights());

        cameraBinder.bind(surface.getProgram(), getContext().getParent().getCamera());

        matrixBinder.bindModelMatrix(surface.getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindViewProjectionMatrix(surface.getProgram(), getContext().getParent().getViewProjectionMatrixBuffer());

        materialBinder.bind(surface.getProgram(), surface.getMaterials());
        elevationBinder.bind(surface.getProgram(), surface.getHeightmap(), surface.getFactor());
        timeBinder.bind(surface.getProgram(), getContext().getParent().getTime());

        geometryHelper.render(surface.getGridMesh().getGeometry()); //TODO: should be a separate shader. Or a separate format

        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class TerrainSurfaceRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneInstanceImplementation scene;

        public TerrainSurfaceRenderContext withModelMatrix(final Matrix4f matrix) {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();

            modelMatrix.identity()
                    .set(matrix);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public TerrainSurfaceRenderContext withScene(final SceneInstanceImplementation scene) {
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
