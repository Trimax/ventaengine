package com.venta.engine.model.view;

import org.joml.Vector3f;

public interface CameraView extends AbstractView {
    Vector3f getPosition();

    void setPosition(final Vector3f position);

    void lookAt(final Vector3f position);
}
