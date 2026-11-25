package io.github.trimax.venta.engine.model.common.water;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.water.WaterMaterialDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class WaterMaterial {
    public static final float DEFAULT_DEPTH_DISTANCE = 8.f;
    public static final float DEFAULT_REFRACTION = 0.04f;
    public static final float DEFAULT_SWASH_PERIOD = 8.f;
    public static final float MAXIMAL_REFRACTION = 0.5f;

    @NonNull
    Vector3f colorSurface;

    @NonNull
    Vector3f colorDepth;

    @NonNull
    Vector3f colorPeak;

    float metalness;
    float opacity;

    /** How deep (along the view, in the units of the world) the water has to be to hide what lies under it almost entirely */
    float depthDistance;

    /** How far the bottom seen through the water is shifted by its ripples, a share of the screen (0 is none) */
    float refraction;

    /** Whether the water mirrors the scene: the scene is drawn once more for the nearest water that has it (see ReflectionRenderer) */
    boolean reflections;

    /** How far from the camera (in the units of the world) the waves are gone: the sea far away is flat, which the eye can't tell from the waves, and the grid is not fine there (0 keeps them) */
    float waveFadeDistance;

    /** How high the water runs up the shore and back, in the units of the world, and how long a run takes, in seconds (0 for none) */
    float swashAmplitude;
    float swashPeriod;

    public WaterMaterial(@NonNull final WaterMaterialDTO dto) {
        this(dto.colorSurface().toVector3f(), dto.colorDepth().toVector3f(), dto.colorPeak().toVector3f(),
                dto.metalness(), dto.opacity(),
                dto.depthDistance() == null ? DEFAULT_DEPTH_DISTANCE : checkDepthDistance(dto.depthDistance()),
                dto.refraction() == null ? DEFAULT_REFRACTION : checkRefraction(dto.refraction()),
                dto.reflections() != null && dto.reflections(),
                dto.waveFadeDistance() == null ? 0.f : checkNotNegative("The distance the waves fade out at", dto.waveFadeDistance()),
                dto.swashAmplitude() == null ? 0.f : checkNotNegative("The amplitude of the swash", dto.swashAmplitude()),
                dto.swashPeriod() == null ? DEFAULT_SWASH_PERIOD : checkSwashPeriod(dto.swashPeriod()));
    }

    public WaterMaterial(@NonNull final WaterMaterial material) {
        this(new Vector3f(material.colorSurface), new Vector3f(material.colorDepth), new Vector3f(material.colorPeak),
                material.getMetalness(), material.getOpacity(), material.getDepthDistance(), material.getRefraction(), material.isReflections(),
                material.getWaveFadeDistance(), material.getSwashAmplitude(), material.getSwashPeriod());
    }

    private static float checkNotNegative(final String what, final float value) {
        if (!(value >= 0.f && Float.isFinite(value)))
            throw new IllegalArgumentException(what + " must not be negative, got " + value);

        return value;
    }

    private static float checkSwashPeriod(final float period) {
        if (!(period > 0.f && Float.isFinite(period)))
            throw new IllegalArgumentException("The period of the swash must be positive, got " + period);

        return period;
    }

    private static float checkDepthDistance(final float depthDistance) {
        if (!(depthDistance > 0.f && Float.isFinite(depthDistance)))
            throw new IllegalArgumentException("The depth distance of water must be positive, got " + depthDistance);

        return depthDistance;
    }

    private static float checkRefraction(final float refraction) {
        if (!(refraction >= 0.f && refraction <= MAXIMAL_REFRACTION))
            throw new IllegalArgumentException(String.format("The refraction of water must be from 0 to %s, got %s", MAXIMAL_REFRACTION, refraction));

        return refraction;
    }
}
