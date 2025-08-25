package io.github.trimax.venta.engine.renderers.entity;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.renderers.instance.SceneInstanceRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CubemapEntityRenderer extends AbstractEntityRenderer<CubemapEntityImplementation, CubemapEntityRenderer.CubemapRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    @Override
    protected CubemapRenderContext createContext() {
        return new CubemapRenderContext();
    }

    @Override
    public void render(final CubemapEntityImplementation cubemap) {
        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);

        glUseProgram(cubemap.getProgram().getInternalID());

        Matrix4f viewNoTranslation = new Matrix4f(getContext().getParent().getCamera().getViewMatrix());
        viewNoTranslation.m30(0).m31(0).m32(0);

        // projection из SceneRenderContext
        getContext().getParent().getViewProjectionMatrix() //TODO: use real projection matrix
        Matrix4f projection = getContext().getParent().getCamera().getProjectionMatrix();

        // Binder call will be here
        skyboxShader.setUniform("matrixView", viewNoTranslation);
        skyboxShader.setUniform("matrixProjection", projection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap.getInternalID());

        //TODO: Create skybox controller? Like console etc
        glBindVertexArray(cubeVao);
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
