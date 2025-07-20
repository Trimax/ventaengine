package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SceneRenderer implements AbstractRenderer<SceneManager.SceneEntity, SceneView> {
    private final ObjectRenderer objectRenderer;

    @Override
    public void render(final SceneView view) {
        view.getObjects().forEach(objectRenderer::render);
    }
}
