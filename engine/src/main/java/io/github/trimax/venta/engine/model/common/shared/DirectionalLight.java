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

    @NonNull
    private Vector3f color;

    @NonNull
    private Vector3f direction;

    private float intensity;

    /** Whether the objects and the terrain cast shadows of this light */
    private boolean castShadows;

    /** How far from the camera the shadows are drawn: the farther, the coarser they are */
    private float shadowDistance = DEFAULT_SHADOW_DISTANCE;

    public DirectionalLight(@NonNull final Vector3f color, @NonNull final Vector3f direction, final float intensity) {
        this(color, direction, intensity, false, DEFAULT_SHADOW_DISTANCE);
    }

    public DirectionalLight(@NonNull final SceneDirectionalLightDTO dto) {
        this(dto.color().toVector3f(), dto.direction(), dto.intensity(), Boolean.TRUE.equals(dto.castShadows()),
                checkShadowDistance(dto.shadowDistance() == null ? DEFAULT_SHADOW_DISTANCE : dto.shadowDistance()));
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
