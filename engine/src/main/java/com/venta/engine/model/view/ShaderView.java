package com.venta.engine.model.view;

import com.venta.engine.managers.ShaderManager;
import com.venta.engine.renderers.AbstractRenderer;

public final class ShaderView extends AbstractRenderer.AbstractView<ShaderManager.ShaderEntity> {
    public ShaderView(final ShaderManager.ShaderEntity entity) {
        super(entity);
    }
}
