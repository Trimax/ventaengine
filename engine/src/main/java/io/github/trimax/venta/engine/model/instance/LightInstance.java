package io.github.trimax.venta.engine.model.instance;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import lombok.NonNull;

public interface LightInstance extends AbstractInstance {
    Vector3fc getPosition();

    float getIntensity();

    Vector3fc getColor();

    Attenuation getAttenuation();

    void setPosition(@NonNull final Vector3fc position);

    void setIntensity(float intensity);

    void setColor(final Vector3fc color);

    void setAttenuation(@NonNull final Attenuation attenuation);

    boolean isEnabled();

    void setEnabled(final boolean value);

    /**
     * How far the light reaches, in the units of the world: it fades smoothly to nothing at this distance, and a light that
     * casts shadows has them up to it. 0 is a light that reaches as far as its attenuation does (it casts no shadows then).
     */
    float getRange();

    void setRange(final float range);

    boolean isCastShadows();

    void setCastShadows(final boolean value);
}
