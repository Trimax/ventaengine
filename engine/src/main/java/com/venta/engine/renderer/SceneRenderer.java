package com.venta.engine.renderer;

import com.venta.engine.annotations.Component;
import com.venta.engine.manager.SceneManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneRenderer implements AbstractRenderer<SceneManager.SceneEntity> {
    private final ObjectRenderer objectRenderer;

    @Override
    public void render(final SceneManager.SceneEntity scene) {
        scene.getObjects().forEach(objectRenderer::render);
    }
}
