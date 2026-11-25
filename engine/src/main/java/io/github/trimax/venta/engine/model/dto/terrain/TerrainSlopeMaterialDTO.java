package io.github.trimax.venta.engine.model.dto.terrain;

import lombok.NonNull;

/**
 * A material that covers a terrain by the steepness of the ground, over the materials of the heights: the rock of a cliff.
 *
 * @param material   the material, sampled from three sides so it doesn't stretch on the steep ground
 * @param minSlope   the angle (in degrees from the horizon) from which the material starts, 0 when it is not set
 * @param maxSlope   the angle up to which the material stays, 90 when it is not set
 * @param slopeBlend how many degrees the material takes to fade outside of the angles, 8 when it is not set
 */
public record TerrainSlopeMaterialDTO(@NonNull String material, Float minSlope, Float maxSlope, Float slopeBlend) {
}
