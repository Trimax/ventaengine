package com.venta.engine.model.view;

import org.joml.Vector3f;
import org.joml.Vector4f;

public interface LightView extends AbstractView {
    Vector3f getPosition();

    Vector3f getDirection();

    Vector4f getColor();

    Attenuation getAttenuation();

    void setPosition(final Vector3f position);

    void setDirection(final Vector3f position);

    void setColor(final Vector4f color);

    void setAttenuation(final Attenuation attenuation);

    record Attenuation(float constant, float linear, float quadratic) {}
}
