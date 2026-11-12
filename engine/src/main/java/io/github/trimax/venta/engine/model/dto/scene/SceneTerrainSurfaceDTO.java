package io.github.trimax.venta.engine.model.dto.scene;

import org.joml.Vector3f;

import lombok.NonNull;

/**
 * A terrain surface of a scene: the angles and the scale are optional.
 */
public record SceneTerrainSurfaceDTO(@NonNull String name,
                                     @NonNull String terrainSurface,
                                     @NonNull Vector3f position,
                                     Vector3f angles,
                                     Vector3f scale) {
}
