package io.github.trimax.venta.engine.model.common.shadow;

import org.joml.Matrix4fc;

import lombok.NonNull;

/**
 * A part of the view frustum with its own shadow map: the nearer the part, the smaller the area of the map and the
 * sharper the shadows.
 *
 * @param matrix       world space to the clip space of the sun for the shadow map of the cascade
 * @param farDistance  the distance from the camera along its view direction where the cascade ends
 * @param texelSize    the size of a texel of the shadow map in world units
 */
public record ShadowCascade(@NonNull Matrix4fc matrix, float farDistance, float texelSize) {
}
