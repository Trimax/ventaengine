package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.scene.SceneAmbientOcclusionDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The ambient occlusion of a scene: what stands close to a surface keeps the light of the sky from it, so the contacts and
 * the corners are darker. It is computed from the depth of the scene (screen space) and multiplies the light of the scene.
 */
@Getter
public final class AmbientOcclusion {
    public static final float DEFAULT_INTENSITY = 0.7f;
    public static final float DEFAULT_RADIUS = 2.f;
    public static final float DEFAULT_POWER = 1.5f;

    public static final float MAXIMAL_POWER = 8.f;

    /** How much of the occlusion is applied, from 0 to 1 */
    private float intensity;

    /** How far around a point the objects darken it, in the units of the world */
    private float radius;

    /** How sharply the occlusion grows: the greater, the darker the deep corners and the lighter the rest */
    private float power;

    public AmbientOcclusion(@NonNull final SceneAmbientOcclusionDTO dto) {
        this(dto.intensity() == null ? DEFAULT_INTENSITY : dto.intensity(),
                dto.radius() == null ? DEFAULT_RADIUS : dto.radius(),
                dto.power() == null ? DEFAULT_POWER : dto.power());
    }

    public AmbientOcclusion(final float intensity, final float radius, final float power) {
        setIntensity(intensity);
        setRadius(radius);
        setPower(power);
    }

    public void setIntensity(final float intensity) {
        if (!(intensity >= 0.f && intensity <= 1.f))
            throw new IllegalArgumentException("The intensity of the ambient occlusion must be from 0 to 1, got " + intensity);

        this.intensity = intensity;
    }

    public void setRadius(final float radius) {
        if (!Float.isFinite(radius) || radius <= 0.f)
            throw new IllegalArgumentException("The radius of the ambient occlusion must be positive, got " + radius);

        this.radius = radius;
    }

    public void setPower(final float power) {
        if (!(power > 0.f && power <= MAXIMAL_POWER))
            throw new IllegalArgumentException(String.format("The power of the ambient occlusion must be positive and at most %s, got %s", MAXIMAL_POWER, power));

        this.power = power;
    }
}
