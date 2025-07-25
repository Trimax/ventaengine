package com.venta.engine.model.view;

import org.joml.Vector3f;

import com.venta.engine.managers.CameraManager;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.Getter;

@Getter
public final class CameraView extends AbstractRenderer.AbstractView<CameraManager.CameraEntity> {
    public CameraView(final CameraManager.CameraEntity entity) {
        super(entity);
    }

    public void setPosition(final Vector3f position) {
        this.entity.setPosition(position);
    }

    public void lookAt(final Vector3f lookAt) {
        this.entity.lookAt(lookAt);
    }
}
