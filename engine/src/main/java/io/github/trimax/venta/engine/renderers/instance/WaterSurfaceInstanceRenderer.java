package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import java.nio.FloatBuffer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.NoiseBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.binders.TimeBinder;
import io.github.trimax.venta.engine.binders.WaterFoamBinder;
import io.github.trimax.venta.engine.binders.WaterMaterialBinder;
import io.github.trimax.venta.engine.binders.WaveBinder;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterSurfaceInstanceRenderer extends
        AbstractInstanceRenderer<WaterSurfaceInstanceImplementation, WaterSurfaceInstanceRenderer.WaterSurfaceRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final WaterMaterialBinder waterMaterialBinder;
    private final WaterFoamBinder waterFoamBinder;
    private final GeometryHelper geometryHelper;
    private final TextureBinder textureBinder;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final NoiseBinder noiseBinder;
    private final LightBinder lightBinder;
    private final WaveBinder waveBinder;
    private final TimeBinder timeBinder;

    @Override
    protected WaterSurfaceRenderContext createContext() {
        return new WaterSurfaceRenderContext();
    }

    @Override
    public void render(final WaterSurfaceInstanceImplementation surface) {
        glUseProgram(surface.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, surface.getDrawMode().getMode());

        lightBinder.bind(surface.getProgram(), getContext().getScene().getAmbientLight());
        lightBinder.bind(surface.getProgram(), getContext().getScene().getLights());

        cameraBinder.bind(surface.getProgram(), getContext().getParent().getCamera());

        matrixBinder.bindModelMatrix(surface.getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindViewProjectionMatrix(surface.getProgram(), getContext().getParent().getViewProjectionMatrixBuffer());

        timeBinder.bind(surface.getProgram(), getContext().getParent().getTime());
        waveBinder.bind(surface.getProgram(), surface.getWaveAmplitude());
        noiseBinder.bind(surface.getProgram(), surface.getNoises());
        waveBinder.bind(surface.getProgram(), surface.getWaves());

        textureBinder.bind(surface.getProgram(), getContext().getScene().getSkybox());

        waterMaterialBinder.bind(surface.getProgram(), surface.getMaterial());
        waterFoamBinder.bind(surface.getProgram(), surface.getFoam());

        geometryHelper.render(surface.getGridMesh().getGeometry());

        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class WaterSurfaceRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneInstanceImplementation scene;

        public WaterSurfaceRenderContext withModelMatrix(final Matrix4f matrix) {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();

            modelMatrix.identity()
                    .set(matrix);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public WaterSurfaceRenderContext withScene(final SceneInstanceImplementation scene) {
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
