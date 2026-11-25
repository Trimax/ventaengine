package io.github.trimax.venta.engine.model.dto.scene;

/**
 * How the camera sees a scene.
 *
 * @param near the distance of the near plane (what is nearer is not drawn), in the units of the world
 * @param far  the distance of the far plane (what is farther is not drawn)
 * @param fov  the vertical field of view, in degrees
 */
public record SceneViewDTO(Float near, Float far, Float fov) {
}
