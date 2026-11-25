package io.github.trimax.venta.engine.model.common.water;

import io.github.trimax.venta.engine.model.dto.water.WaterFoamDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class WaterFoam {
    public static final float DEFAULT_SHORE_WIDTH = 1.2f;
    public static final float DEFAULT_SHORE_INTENSITY = 0.8f;

    private float threshold;
    private float intensity;

    /** How far from the shore (and from whatever stands in the water), in the units of the world, the water is covered with foam (0 is none) */
    private float shoreWidth;

    /** How white the foam of the shore is, from 0 to 1 */
    private float shoreIntensity;

    /** How white the lines of foam are that run to the shore, one after another, from the deep, from 0 (none) to 1 */
    private float bands;

    public WaterFoam(@NonNull final WaterFoamDTO dto) {
        this(dto.threshold(), dto.intensity(),
                dto.shoreWidth() == null ? DEFAULT_SHORE_WIDTH : checkShoreWidth(dto.shoreWidth()),
                dto.shoreIntensity() == null ? DEFAULT_SHORE_INTENSITY : checkShoreIntensity(dto.shoreIntensity()),
                dto.bands() == null ? 0.f : checkBands(dto.bands()));
    }

    public WaterFoam(@NonNull final WaterFoam foam) {
        this(foam.getThreshold(), foam.getIntensity(), foam.getShoreWidth(), foam.getShoreIntensity(), foam.getBands());
    }

    private static float checkBands(final float bands) {
        if (!(bands >= 0.f && bands <= 1.f))
            throw new IllegalArgumentException("The intensity of the bands of foam must be from 0 to 1, got " + bands);

        return bands;
    }

    private static float checkShoreWidth(final float shoreWidth) {
        if (!(shoreWidth >= 0.f && Float.isFinite(shoreWidth)))
            throw new IllegalArgumentException("The width of the foam of the shore must not be negative, got " + shoreWidth);

        return shoreWidth;
    }

    private static float checkShoreIntensity(final float shoreIntensity) {
        if (!(shoreIntensity >= 0.f && shoreIntensity <= 1.f))
            throw new IllegalArgumentException("The intensity of the foam of the shore must be from 0 to 1, got " + shoreIntensity);

        return shoreIntensity;
    }
}
