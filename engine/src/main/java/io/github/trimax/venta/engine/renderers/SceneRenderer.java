package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.model.entities.CameraEntity;
import io.github.trimax.venta.engine.model.entities.SceneEntity;
import io.github.trimax.venta.engine.model.entities.WindowEntity;
import io.github.trimax.venta.engine.model.view.CameraView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import lombok.*;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneRenderer extends AbstractRenderer<SceneEntity, SceneRenderer.SceneRenderContext, SceneRenderer.SceneRenderContext> {
    private final ObjectManagerImplementation objectManager;
    private final ObjectRenderer objectRenderer;

    @Override
    protected SceneRenderContext createContext() {
        return new SceneRenderContext();
    }

    @Override
    @SneakyThrows
    public void render(final SceneEntity scene) {
        if (scene == null)
            return;

        for (final ObjectView object : scene.getObjects())
            try (final var _ = objectRenderer.withContext(getContext())
                    .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())
                    .withScene(scene)) {
                objectRenderer.render(objectManager.getEntity(object.getID()));
            }
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext<SceneRenderContext> {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();
        private CameraView camera;

        public SceneRenderContext with(final WindowEntity window, final CameraEntity camera) {
            window.getProjectionMatrix().mul(camera.getViewMatrix(), viewProjectionMatrix);
            viewProjectionMatrix.get(viewProjectionMatrixBuffer);
            this.camera = camera;

            return this;
        }

        @Override
        public void close() {
            viewProjectionMatrixBuffer.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
        }
    }
}
