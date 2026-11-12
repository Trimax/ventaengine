package io.github.trimax.venta.engine.model.dto.scene;

import org.joml.Vector3f;

import lombok.NonNull;

/**
 * A water surface of a scene: the angles and the scale are optional.
 */
public record SceneWaterSurfaceDTO(@NonNull String name,
                                   @NonNull String waterSurface,
                                   @NonNull Vector3f position,
                                   Vector3f angles,
                                   Vector3f scale) {
}
