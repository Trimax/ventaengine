package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneDirectionalLightDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class DirectionalLight {
    public static final float DEFAULT_SHADOW_DISTANCE = 200.f;
    public static final float DEFAULT_DISC_INTENSITY = 40.f;
    public static final float MAXIMAL_DISC_SIZE = 30.f;

    @NonNull
    private Vector3f color;

    @NonNull
    private Vector3f direction;

    private float intensity;

    /** Whether the objects and the terrain cast shadows of this light */
    private boolean castShadows;

    /** How far from the camera the shadows are drawn: the farther, the coarser they are */
    private float shadowDistance = DEFAULT_SHADOW_DISTANCE;

    /** The angular radius of the disc of the sun in the sky in degrees, 0 draws no disc (the real sun is about a quarter of a degree) */
    private float discSize;

    /** How much brighter than the light the disc is: the disc is far brighter than what it lights, so it glows with bloom */
    private float discIntensity = DEFAULT_DISC_INTENSITY;

    public DirectionalLight(@NonNull final Vector3f color, @NonNull final Vector3f direction, final float intensity) {
        this(color, direction, intensity, false, DEFAULT_SHADOW_DISTANCE, 0.f, DEFAULT_DISC_INTENSITY);
    }

    public DirectionalLight(@NonNull final SceneDirectionalLightDTO dto) {
        this(dto.color().toVector3f(), dto.direction(), dto.intensity(), Boolean.TRUE.equals(dto.castShadows()),
                checkShadowDistance(dto.shadowDistance() == null ? DEFAULT_SHADOW_DISTANCE : dto.shadowDistance()),
                checkDiscSize(dto.discSize() == null ? 0.f : dto.discSize()),
                checkDiscIntensity(dto.discIntensity() == null ? DEFAULT_DISC_INTENSITY : dto.discIntensity()));
    }

    public void setDiscSize(final float discSize) {
        this.discSize = checkDiscSize(discSize);
    }

    public void setDiscIntensity(final float discIntensity) {
        this.discIntensity = checkDiscIntensity(discIntensity);
    }

    private static float checkDiscSize(final float discSize) {
        if (!(discSize >= 0.f && discSize <= MAXIMAL_DISC_SIZE))
            throw new IllegalArgumentException("The size of the disc of the sun must be from 0 to " + MAXIMAL_DISC_SIZE + " degrees, got " + discSize);

        return discSize;
    }

    private static float checkDiscIntensity(final float discIntensity) {
        if (!Float.isFinite(discIntensity) || discIntensity < 0.f)
            throw new IllegalArgumentException("The intensity of the disc of the sun must not be negative, got " + discIntensity);

        return discIntensity;
    }

    public void setShadowDistance(final float shadowDistance) {
        this.shadowDistance = checkShadowDistance(shadowDistance);
    }

    private static float checkShadowDistance(final float shadowDistance) {
        if (!(shadowDistance > 0.f) || Float.isInfinite(shadowDistance))
            throw new IllegalArgumentException("The shadow distance must be positive and finite, got " + shadowDistance);

        return shadowDistance;
    }
}
