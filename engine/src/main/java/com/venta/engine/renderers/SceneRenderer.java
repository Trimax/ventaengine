package com.venta.engine.renderers;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.ObjectView;
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

        for (final ObjectView object : scene.getObjects())
            try (final var _ = objectRenderer.getContext()
                    .withViewProjectionMatrix(getContext().viewProjectionMatrixBuffer)
                    .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())
                    .withScene(scene)) {
                objectRenderer.render(object);
            }
    }

    @Getter(AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();

        public SceneRenderContext with(final WindowView window, final CameraView camera) {
            window.entity.getProjectionMatrix().mul(camera.entity.getViewMatrix(), viewProjectionMatrix);
            viewProjectionMatrix.get(viewProjectionMatrixBuffer);
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
