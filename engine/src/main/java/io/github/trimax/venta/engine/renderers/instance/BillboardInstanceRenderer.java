package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.SpriteBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.BillboardInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BillboardInstanceRenderer extends AbstractInstanceRenderer<BillboardInstanceImplementation, BillboardInstanceRenderer.BillboardRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final GeometryHelper geometryHelper;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final SpriteBinder spriteBinder;
    private final FogBinder fogBinder;

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

        cameraBinder.bind(billboard.getProgram(), getContext().getParent().getCamera());
        fogBinder.bind(billboard.getProgram(), getContext().getScene().getFog());
        spriteBinder.bindSprite(billboard.getProgram(), billboard.getSprite(), getFrameIndex(billboard.getSprite()));

        geometryHelper.render(billboard.getGeometry());

        glUseProgram(0);
    }

    private int getFrameIndex(final SpriteEntityImplementation sprite) {
        final var time = getContext().getParent().getTime().getTimeElapsed();

        final var frame = (int) (time / sprite.getDuration());

        return sprite.isLooping() ? frame % sprite.getFrameCount() : Math.min(frame, sprite.getFrameCount() - 1);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class BillboardRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewMatrix = new Matrix4f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private SceneInstanceImplementation scene;

        public BillboardRenderContext withScene(final SceneInstanceImplementation scene) {
            this.scene = scene;
            return this;
        }

        public BillboardRenderContext withModelMatrix(final Matrix4f matrix) {
            modelMatrixBuffer.clear();

            modelMatrix.identity().set(matrix);
            applyBillboard(modelMatrix, getParent().getViewMatrix());
            modelMatrix.get(modelMatrixBuffer);

            return this;
        }

        private void applyBillboard(final Matrix4f model, final Matrix4fc view) {
            viewMatrix.set(view);
            viewMatrix.m30(0);
            viewMatrix.m31(0);
            viewMatrix.m32(0);

            viewMatrix.invert();
            model.mul(viewMatrix);
        }

        @Override
        public void close() {
            modelMatrixBuffer.clear();
            scene = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
