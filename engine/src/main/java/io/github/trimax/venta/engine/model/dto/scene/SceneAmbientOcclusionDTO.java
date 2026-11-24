package io.github.trimax.venta.engine.model.dto.scene;

/**
 * The ambient occlusion of a scene: the corners, the contacts of walls with the ground and the roots of the trees are
 * darker, because the light of the sky and the light bounced by the land reach them less.
 *
 * @param intensity how much of the occlusion is applied, from 0 (none) to 1 (all of it)
 * @param radius    how far around a point (in the units of the world) the objects darken it
 * @param power     how sharply the occlusion grows: the greater, the darker the deep corners and the lighter the rest
 */
public record SceneAmbientOcclusionDTO(Float intensity, Float radius, Float power) {
}
