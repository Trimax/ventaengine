package io.github.trimax.venta.engine.model.common.terrain;

import io.github.trimax.venta.engine.model.dto.terrain.TerrainWetnessDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The wet ground of a shore: below the water, and as high above it as the swash of the water runs up, the ground is wet. It is
 * darker and smoother there, and it dries out slowly higher up. The level, the amplitude and the period are the ones of the water,
 * so the wet line goes up and down with it.
 */
@Getter
public final class TerrainWetness {
    public static final float DEFAULT_HEIGHT = 0.6f;
    public static final float DEFAULT_DARKENING = 0.45f;
    public static final float DEFAULT_GLOSSINESS = 0.6f;
    public static final float DEFAULT_SWASH_PERIOD = 8.f;

    private final float level;
    private final float height;
    private final float darkening;
    private final float glossiness;
    private final float swashAmplitude;
    private final float swashPeriod;

    public TerrainWetness(@NonNull final TerrainWetnessDTO dto) {
        this(dto.level() == null ? 0.f : dto.level(),
                dto.height() == null ? DEFAULT_HEIGHT : dto.height(),
                dto.darkening() == null ? DEFAULT_DARKENING : dto.darkening(),
                dto.glossiness() == null ? DEFAULT_GLOSSINESS : dto.glossiness(),
                dto.swashAmplitude() == null ? 0.f : dto.swashAmplitude(),
                dto.swashPeriod() == null ? DEFAULT_SWASH_PERIOD : dto.swashPeriod());
    }

    public TerrainWetness(final float level, final float height, final float darkening, final float glossiness, final float swashAmplitude, final float swashPeriod) {
        if (!Float.isFinite(level))
            throw new IllegalArgumentException("The level of the water must be a number, got " + level);

        if (!(height > 0.f && Float.isFinite(height)))
            throw new IllegalArgumentException("The height of the wet ground must be positive, got " + height);

        if (!(darkening >= 0.f && darkening <= 1.f))
            throw new IllegalArgumentException("The darkening of the wet ground must be from 0 to 1, got " + darkening);

        if (!(glossiness >= 0.f && glossiness <= 1.f))
            throw new IllegalArgumentException("The glossiness of the wet ground must be from 0 to 1, got " + glossiness);

        if (!(swashAmplitude >= 0.f && Float.isFinite(swashAmplitude)))
            throw new IllegalArgumentException("The amplitude of the swash must not be negative, got " + swashAmplitude);

        if (!(swashPeriod > 0.f && Float.isFinite(swashPeriod)))
            throw new IllegalArgumentException("The period of the swash must be positive, got " + swashPeriod);

        this.level = level;
        this.height = height;
        this.darkening = darkening;
        this.glossiness = glossiness;
        this.swashAmplitude = swashAmplitude;
        this.swashPeriod = swashPeriod;
    }
}
