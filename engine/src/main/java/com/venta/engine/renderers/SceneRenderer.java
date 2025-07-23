package com.venta.engine.renderers;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.SceneView;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneRenderer extends AbstractRenderer<SceneManager.SceneEntity, SceneView, SceneRenderer.SceneRenderContext> {
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

        try (final var _ = objectRenderer.getContext()
                .withProjectionMatrix(getContext().projectionMatrixBuffer)
                .withViewMatrix(getContext().viewMatrixBuffer)
                .withScene(scene)) {
            scene.getObjects().forEach(objectRenderer::render);
        }
    }

    @Getter(AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext {
        private final FloatBuffer projectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer viewMatrixBuffer = MemoryUtil.memAllocFloat(16);

        public SceneRenderContext withWindow(final WindowView window) {
            window.entity.getProjectionMatrix().get(projectionMatrixBuffer);
            return this;
        }

        public SceneRenderContext withCamera(final CameraView camera) {
            camera.entity.getViewMatrix().get(viewMatrixBuffer);
            return this;
        }

        @Override
        public void close() {
            projectionMatrixBuffer.clear();
            viewMatrixBuffer.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }
}
