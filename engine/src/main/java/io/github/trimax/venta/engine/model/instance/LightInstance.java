package io.github.trimax.venta.engine.model.instance;

import org.joml.Vector3f;

public interface LightInstance extends AbstractInstance {
    Vector3f getPosition();

    Vector3f getDirection();

    float getIntensity();

    Vector3f getColor();

    Attenuation getAttenuation();

    void setPosition(final Vector3f position);

    void setDirection(final Vector3f position);

    void setIntensity(float intensity);

    void setColor(final Vector3f color);

    void setAttenuation(final Attenuation attenuation);

    record Attenuation(float constant, float linear, float quadratic) {}
}
