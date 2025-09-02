package io.github.trimax.venta.engine.renderers.entity;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.renderers.instance.SceneInstanceRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CubemapEntityRenderer extends AbstractEntityRenderer<CubemapEntityImplementation, CubemapEntityRenderer.CubemapRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final FloatBuffer viewMatrixWithoutTranslationBuffer = MemoryUtil.memAllocFloat(16);
    private final Matrix4f viewMatrixWithoutTranslation = new Matrix4f();

    private final TextureBinder textureBinder;
    private final MatrixBinder matrixBinder;
    private final FogBinder fogBinder;

    @Override
    protected CubemapRenderContext createContext() {
        return new CubemapRenderContext();
    }

    @Override
    public void render(final CubemapEntityImplementation cubemap) {
        glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Polygon.getMode());

        final var program = cubemap.getProgram();

        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);

        glUseProgram(program.getInternalID());

        viewMatrixWithoutTranslationBuffer.clear();
        viewMatrixWithoutTranslation.set(getContext().getParent().getViewMatrix());
        viewMatrixWithoutTranslation.m30(0).m31(0).m32(0);
        viewMatrixWithoutTranslation.get(viewMatrixWithoutTranslationBuffer);

        matrixBinder.bindProjectionMatrix(program, getContext().getParent().getProjectionMatrixBuffer());
        matrixBinder.bindViewMatrix(program, viewMatrixWithoutTranslationBuffer);

        fogBinder.bind(program, getContext().getScene().getFog());
        textureBinder.bind(program, cubemap);

        glBindVertexArray(cubemap.getVertexArrayObjectID());
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        glUseProgram(0);

        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class CubemapRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private SceneInstance scene;

        public CubemapRenderContext withScene(final SceneInstance scene) {
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
