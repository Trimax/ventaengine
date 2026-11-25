package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.LightDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The cone of a spot light: the angles are measured from its direction to the side, in degrees. The light is full inside
 * the inner angle and fades smoothly to nothing at the outer one.
 *
 * <p>The outer angle is at most {@link #MAXIMAL_OUTER_ANGLE}: a shadow map is a perspective view of the cone, and it can't
 * hold one that is nearly flat.
 */
@Getter
public final class Cone {
    public static final float DEFAULT_INNER_ANGLE = 20.f;
    public static final float DEFAULT_OUTER_ANGLE = 30.f;
    public static final float MAXIMAL_OUTER_ANGLE = 85.f;

    private final float innerAngle;
    private final float outerAngle;

    public Cone(@NonNull final LightDTO dto) {
        this(dto.innerAngle() == null ? DEFAULT_INNER_ANGLE : dto.innerAngle(),
                dto.outerAngle() == null ? Math.max(DEFAULT_OUTER_ANGLE, dto.innerAngle() == null ? 0.f : dto.innerAngle()) : dto.outerAngle());
    }

    public Cone(final float innerAngle, final float outerAngle) {
        if (!(innerAngle > 0.f && innerAngle <= outerAngle && outerAngle <= MAXIMAL_OUTER_ANGLE))
            throw new IllegalArgumentException(String.format("The angles of a cone must be 0 < inner <= outer <= %s degrees, got %s and %s",
                    MAXIMAL_OUTER_ANGLE, innerAngle, outerAngle));

        this.innerAngle = innerAngle;
        this.outerAngle = outerAngle;
    }

    /** The cosine of the inner angle: what is more aligned with the direction than this gets the full light */
    public float getCosInner() {
        return (float) Math.cos(Math.toRadians(innerAngle));
    }

    /** The cosine of the outer angle: what is less aligned with the direction than this gets none */
    public float getCosOuter() {
        return (float) Math.cos(Math.toRadians(outerAngle));
    }
}
