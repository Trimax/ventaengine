package io.github.trimax.venta.engine.model.dto.scene;

import org.joml.Vector3f;

/**
 * The wind of a scene: it blows along the ground, so only the x and z of the direction are used. The strength is how far
 * (in the units of the world) the top of a surface that fully follows the wind (see the wind influence of a material) is
 * pushed, the speed is how fast the gusts follow each other.
 */
public record SceneWindDTO(Vector3f direction, Float strength, Float speed) {
}
