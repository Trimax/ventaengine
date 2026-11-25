package io.github.trimax.venta.engine.model.dto;

/**
 * The dynamic reflections of a material: every object with the material gets a cube map of its surroundings, drawn from its
 * position, and reflects it instead of the skybox.
 *
 * @param resolution the size of a face of the cube map in pixels
 * @param refresh    the seconds between the starts of two drawings of the cube map, 0 draws it once and keeps it
 * @param range      the distance to the camera up to which the object has its cube map: farther it reflects the skybox
 */
public record MaterialReflectionsDTO(Integer resolution, Float refresh, Float range) {
}
