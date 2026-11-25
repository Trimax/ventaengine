package io.github.trimax.venta.engine.model.instance;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.common.shared.Cone;
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

    LightType getType();

    /** The cone of a spot light, null for a point light */
    Cone getCone();

    /** The unit vector a spot light shines along, it is down at first */
    Vector3fc getDirection();

    /** Turns the light into a spot light with a cone (or into a point light, when the cone is null) */
    void setCone(final Cone cone);

    /** Turns a spot light: the direction is normalized, and it must not be zero or not a number */
    void setDirection(@NonNull final Vector3fc direction);

    boolean isCastShadows();

    void setCastShadows(final boolean value);
}
