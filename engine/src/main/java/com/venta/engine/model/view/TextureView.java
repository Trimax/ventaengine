package com.venta.engine.model.view;

import com.venta.engine.managers.TextureManager;
import com.venta.engine.renderers.AbstractRenderer;

public final class TextureView extends AbstractRenderer.AbstractView<TextureManager.TextureEntity> {
    public TextureView(final TextureManager.TextureEntity entity) {
        super(entity);
    }
}
