package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import org.joml.Vector3f;

public interface LightInstance extends AbstractInstance {
    LightType getType();

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

    boolean isEnabled();

    void setEnabled(final boolean value);

    boolean isCastShadows();

    void setCastShadows(final boolean value);
}
