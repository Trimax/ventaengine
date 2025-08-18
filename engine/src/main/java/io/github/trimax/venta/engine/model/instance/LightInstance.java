package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface LightInstance extends AbstractInstance {
    LightType getType();

    Vector3fc getPosition();

    Vector3fc getDirection();

    float getIntensity();

    Vector3fc getColor();

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
