package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import io.github.trimax.venta.engine.model.view.CameraView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.SceneView;
import lombok.*;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneRenderer extends AbstractRenderer<SceneView, SceneRenderer.SceneRenderContext, SceneRenderer.SceneRenderContext> {
    private final ObjectRenderer objectRenderer;

    @Override
    protected SceneRenderContext createContext() {
        return new SceneRenderContext();
    }

    @Override
    @SneakyThrows
    public void render(final SceneView scene) {
        if (scene == null)
            return;

        for (final ObjectView object : scene.getObjects())
            try (final var _ = objectRenderer.withContext(getContext())
                    .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())
                    .withScene(scene)) {
                objectRenderer.render(object);
            }
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext<SceneRenderContext> {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();
        private CameraView camera;

        public SceneRenderContext with(final WindowManagerImplementation.WindowEntity window, final CameraManagerImplementation.CameraEntity camera) {
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
