package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUniform1i;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import java.nio.FloatBuffer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.ShadowBinder;
import io.github.trimax.venta.engine.binders.NoiseBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.binders.TimeBinder;
import io.github.trimax.venta.engine.binders.WaterFoamBinder;
import io.github.trimax.venta.engine.binders.WaterMaterialBinder;
import io.github.trimax.venta.engine.binders.WaveBinder;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.renderers.common.PostProcessingRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterSurfaceInstanceRenderer extends
        AbstractInstanceRenderer<WaterSurfaceInstanceImplementation, WaterSurfaceInstanceRenderer.WaterSurfaceRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final WaterMaterialBinder waterMaterialBinder;
    private final PostProcessingRenderer postProcessingRenderer;
    private final WaterFoamBinder waterFoamBinder;
    private final GeometryHelper geometryHelper;
    private final TextureBinder textureBinder;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final ShadowBinder shadowBinder;
    private final NoiseBinder noiseBinder;
    private final LightBinder lightBinder;
    private final WaveBinder waveBinder;
    private final TimeBinder timeBinder;
    private final FogBinder fogBinder;

    @Override
    protected WaterSurfaceRenderContext createContext() {
        return new WaterSurfaceRenderContext();
    }

    @Override
    public void render(final WaterSurfaceInstanceImplementation surface) {
        glUseProgram(surface.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, surface.getDrawMode().getMode());

        if (getContext().getParent().markLightsBound(surface.getProgram())) {
            lightBinder.bind(surface.getProgram(), getContext().getScene().getDirectionalLight());
            lightBinder.bind(surface.getProgram(), getContext().getScene().getAmbientLight());
            lightBinder.bind(surface.getProgram(), getContext().getScene().getLights());
            shadowBinder.bind(surface.getProgram());
        }

        cameraBinder.bind(surface.getProgram(), getContext().getParent().getCamera());

        matrixBinder.bindModelMatrix(surface.getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindNormalMatrix(surface.getProgram(), getContext().getNormalMatrixBuffer());
        matrixBinder.bindViewProjectionMatrix(surface.getProgram(), getContext().getParent().getViewProjectionMatrixBuffer());
        matrixBinder.bindViewProjectionInverseMatrix(surface.getProgram(), getContext().getParent().getViewProjectionInverseMatrixBuffer());
        bindSceneTextures(surface);

        timeBinder.bind(surface.getProgram(), getContext().getParent().getTime());
        waveBinder.bind(surface.getProgram(), surface.getWaveAmplitude());
        noiseBinder.bind(surface.getProgram(), surface.getNoises());
        waveBinder.bind(surface.getProgram(), surface.getWaves());

        textureBinder.bind(surface.getProgram(), getContext().getScene().getSkybox());

        waterMaterialBinder.bind(surface.getProgram(), surface.getMaterial());
        waterFoamBinder.bind(surface.getProgram(), surface.getFoam());
        fogBinder.bind(surface.getProgram(), getContext().getScene().getFog(), getContext().getScene().getDirectionalLight());

        geometryHelper.render(surface.getGridMesh().getGeometry());

        glUseProgram(0);
    }

    /** The opaque scene behind the water: its depth gives the depth of the water, its color is what is seen through it */
    private void bindSceneTextures(final WaterSurfaceInstanceImplementation surface) {
        final var program = surface.getProgram();
        final var resolved = getContext().getParent().isSceneResolved();

        glUniform1i(program.getUniformID(ShaderUniform.UseSceneTextures), resolved ? 1 : 0);
        glUniform1i(program.getUniformID(ShaderUniform.TextureSceneColor), TextureUnit.SceneColor.getId());
        glUniform1i(program.getUniformID(ShaderUniform.TextureSceneDepth), TextureUnit.SceneDepth.getId());
        if (!resolved)
            return;

        glActiveTexture(TextureUnit.SceneColor.getLocationID());
        glBindTexture(GL_TEXTURE_2D, postProcessingRenderer.getResolvedTextureID());
        glActiveTexture(TextureUnit.SceneDepth.getLocationID());
        glBindTexture(GL_TEXTURE_2D, postProcessingRenderer.getResolvedDepthTextureID());
        glActiveTexture(GL_TEXTURE0);
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
