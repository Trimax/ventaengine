package com.venta.engine.model.view;

import com.venta.engine.managers.CameraManager;
import com.venta.engine.renderers.AbstractRenderer;

public final class CameraView extends AbstractRenderer.AbstractView<CameraManager.CameraEntity> {
    public CameraView(final String id, final CameraManager.CameraEntity entity) {
        super(id, entity);
    }
}
