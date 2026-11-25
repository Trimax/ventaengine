package io.github.trimax.venta.engine.model.common.terrain;

import io.github.trimax.venta.engine.model.dto.terrain.TerrainSlopeMaterialDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * Where a material of a terrain covers it by the steepness of the ground: the angle between the ground and the horizon,
 * in degrees. The material is full between the angles and fades to nothing over the blend outside of them, so the rock on
 * a cliff doesn't start with an edge.
 */
@Getter
public final class TerrainSlope {
    public static final float DEFAULT_MINIMUM = 0.f;
    public static final float DEFAULT_MAXIMUM = 90.f;
    public static final float DEFAULT_BLEND = 8.f;

    /** The most overlays of slopes a terrain has */
    public static final int MAXIMAL_COUNT = 2;

    private final float minimum;
    private final float maximum;
    private final float blend;

    public TerrainSlope(@NonNull final TerrainSlopeMaterialDTO dto) {
        this(dto.minSlope() == null ? DEFAULT_MINIMUM : dto.minSlope(),
                dto.maxSlope() == null ? DEFAULT_MAXIMUM : dto.maxSlope(),
                dto.slopeBlend() == null ? DEFAULT_BLEND : dto.slopeBlend());
    }

    public TerrainSlope(final float minimum, final float maximum, final float blend) {
        if (!(minimum >= 0.f && minimum <= maximum && maximum <= 90.f))
            throw new IllegalArgumentException(String.format("The slope of a material must be 0 <= min <= max <= 90 degrees, got %s and %s", minimum, maximum));

        if (!(blend >= 0.f && blend <= 90.f))
            throw new IllegalArgumentException("The blend of a slope must be from 0 to 90 degrees, got " + blend);

        this.minimum = minimum;
        this.maximum = maximum;
        this.blend = blend;
    }
}
