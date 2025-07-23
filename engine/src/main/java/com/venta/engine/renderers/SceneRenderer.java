package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneRenderer extends AbstractRenderer<SceneManager.SceneEntity, SceneView> {
    private final ObjectRenderer objectRenderer;
    private final WindowManager windowManager;
    private final CameraManager cameraManager;

    @Override
    @SneakyThrows
    public void render(final SceneView scene) {
        if (scene == null)
            return;

        try (final var _ = objectRenderer.withContext(new RenderContext(cameraManager.getCurrent(), windowManager.getCurrent(), scene.getLights(), scene.getAmbientLight()))) {
            scene.getObjects().forEach(objectRenderer::render);
        }
    }
}
