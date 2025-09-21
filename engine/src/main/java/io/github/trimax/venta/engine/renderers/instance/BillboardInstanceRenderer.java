package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.SpriteBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.BillboardInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BillboardInstanceRenderer extends AbstractInstanceRenderer<BillboardInstanceImplementation, BillboardInstanceRenderer.BillboardRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final MatrixBinder matrixBinder;
    private final SpriteBinder spriteBinder;

    @Override
    protected BillboardRenderContext createContext() {
        return new BillboardRenderContext();
    }

    @Override
    public void render(final BillboardInstanceImplementation billboard) {
        glUseProgram(billboard.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Polygon.getMode());

        matrixBinder.bindModelMatrix(billboard.getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindViewProjectionMatrix(billboard.getProgram(), getContext().getParent().getViewProjectionMatrixBuffer());

        spriteBinder.bindSprite(billboard.getProgram(), billboard.getSprite(), getFrameIndex(billboard.getSprite()));

        render(billboard.getPrefab().getGeometry());

        glUseProgram(0);
    }

    private void render(final Geometry geometry) {
        glBindVertexArray(geometry.objectID());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.facets().id());
        glDrawElements(GL_TRIANGLES, geometry.facets().length(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }

    private int getFrameIndex(final SpriteEntityImplementation sprite) {
        final var time = getContext().getParent().getTime().getTimeElapsed();

        return (int) ((time / sprite.getDuration()) % sprite.getFrameCount());
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class BillboardRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f modelMatrix = new Matrix4f();

        public BillboardRenderContext withModelMatrix(final Matrix4f matrix) {
            modelMatrixBuffer.clear();

            modelMatrix.identity().set(matrix);
            applyBillboard(modelMatrix, getParent().getViewMatrix());
            modelMatrix.get(modelMatrixBuffer);

            return this;
        }

        private void applyBillboard(final Matrix4f model, final Matrix4fc viewMatrix) {
            final var billboardRotation = new Matrix4f(viewMatrix);
            billboardRotation.m30(0);
            billboardRotation.m31(0);
            billboardRotation.m32(0);

            billboardRotation.invert();
            model.mul(billboardRotation);
        }

        @Override
        public void close() {
            modelMatrixBuffer.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
