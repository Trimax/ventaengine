package io.github.trimax.venta.engine.model.common.shadow;

import org.joml.Matrix4f;

import lombok.Getter;

/**
 * A part of the view frustum with its own shadow map: the nearer the part, the smaller the area of the map and the
 * sharper the shadows.
 *
 * <p>A cascade is computed every frame, so it is a holder that is made once and filled again (see {@link ShadowFrame}).
 */
public final class ShadowCascade {
    /** World space to the clip space of the sun for the shadow map of the cascade */
    @Getter
    private final Matrix4f matrix = new Matrix4f();

    /** The distance from the camera along its view direction where the cascade ends */
    @Getter
    private float farDistance;

    /** The size of a texel of the shadow map in world units */
    @Getter
    private float texelSize;

    /**
     * Sets what is not in the matrix, the matrix itself is filled through {@link #getMatrix()}.
     */
    public void set(final float farDistance, final float texelSize) {
        this.farDistance = farDistance;
        this.texelSize = texelSize;
    }
}
