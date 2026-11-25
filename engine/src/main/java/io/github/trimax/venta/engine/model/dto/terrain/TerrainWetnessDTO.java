package io.github.trimax.venta.engine.model.dto.terrain;

/**
 * The wet sand of a shore: the ground that the water has wet is darker and smoother.
 *
 * @param level          the height of the water, in the world
 * @param height         how far above the water (in the units of the world) the wet part fades into the dry one
 * @param darkening      how much darker the wet ground is, from 0 to 1
 * @param glossiness     how much smoother (less rough) the wet ground is, from 0 to 1
 * @param swashAmplitude how high the water runs up the shore, the same as the one of the water (0 for none)
 * @param swashPeriod    how long a run takes, in seconds, the same as the one of the water
 */
public record TerrainWetnessDTO(Float level, Float height, Float darkening, Float glossiness, Float swashAmplitude, Float swashPeriod) {
}
