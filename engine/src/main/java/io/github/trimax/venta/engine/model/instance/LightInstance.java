package io.github.trimax.venta.engine.model.instance;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.dto.common.Attenuation;
import lombok.NonNull;

public interface LightInstance extends AbstractInstance {
    LightType getType();

    Vector3fc getPosition();

    Vector3fc getDirection();

    float getIntensity();

    Vector3fc getColor();

    Attenuation getAttenuation();

    void setPosition(@NonNull final Vector3fc position);

    void setDirection(@NonNull final Vector3fc direction);

    void setIntensity(float intensity);

    void setColor(final Vector3fc color);

    void setAttenuation(@NonNull final Attenuation attenuation);

    boolean isEnabled();

    void setEnabled(final boolean value);

    boolean isCastShadows();

    void setCastShadows(final boolean value);
}
