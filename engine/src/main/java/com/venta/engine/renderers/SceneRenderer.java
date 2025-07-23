package com.venta.engine.renderers;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.SceneView;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneRenderer extends AbstractRenderer<SceneManager.SceneEntity, SceneView, SceneRenderer.SceneRenderContext> {
    private final ObjectRenderer objectRenderer;
    private final WindowManager windowManager;
    private final CameraManager cameraManager;

    @Override
    @SneakyThrows
    public void render(final SceneView scene) {
        if (scene == null)
            return;

        try (final var _ = objectRenderer.withContext(ObjectRenderer.ObjectRenderContext.builder()
                .projectionMatrixBuffer(createProjectionMatrixBuffer(windowManager.getCurrent()))
                .viewMatrixBuffer(createViewMatrixBuffer(cameraManager.getCurrent()))
                .ambientLight(scene.getAmbientLight())
                .lights(scene.getLights())
                .build())) {
            scene.getObjects().forEach(objectRenderer::render);
        }
    }

    private FloatBuffer createProjectionMatrixBuffer(final WindowView window) {
        final var projectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        window.entity.getProjectionMatrix().get(projectionMatrixBuffer);

        return projectionMatrixBuffer;
    }

    private FloatBuffer createViewMatrixBuffer(final CameraView camera) {
        final var viewMatrixBuffer = MemoryUtil.memAllocFloat(16);
        camera.entity.getViewMatrix().get(viewMatrixBuffer);

        return viewMatrixBuffer;
    }

    @SuperBuilder
    @Getter(AccessLevel.PACKAGE)
    static final class SceneRenderContext extends AbstractRenderContext {
        @Override
        public void close() {
        }
    }
}
