package io.github.trimax.venta.engine.model.common.shadow;

import java.util.List;

import org.joml.Vector3fc;

import lombok.NonNull;

/**
 * The shadows of a frame: the cascades of the shadow map and what the shaders need to choose a cascade of a point.
 *
 * @param cascades      the cascades from the nearest to the farthest
 * @param distance      how far from the camera there are shadows, they fade out at the end
 * @param cameraForward the view direction of the camera: the distance of a point along it chooses the cascade
 */
public record ShadowFrame(@NonNull List<ShadowCascade> cascades, float distance, @NonNull Vector3fc cameraForward) {
}
