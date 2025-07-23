package com.venta.engine.renderers;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.SceneView;
import com.venta.engine.model.view.WindowView;
import com.venta.engine.utils.MatrixUtil;
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
                .withViewProjectionMatrix(getContext().viewProjectionMatrixBuffer)
                .withScene(scene)) {
            scene.getObjects().forEach(objectRenderer::render);
        }
    }

    @Getter(AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);

        public SceneRenderContext with(final WindowView window, final CameraView camera) {
            MatrixUtil.createViewProjectionMatrix(window.entity.getProjectionMatrix(), camera.entity.getViewMatrix())
                    .get(viewProjectionMatrixBuffer);
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
