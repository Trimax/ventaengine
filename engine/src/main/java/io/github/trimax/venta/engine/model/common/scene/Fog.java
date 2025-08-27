package io.github.trimax.venta.engine.model.common.scene;

import lombok.Data;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Data
public final class Fog {
    private float minimalDistance;
    private float maximalDistance;
    private Vector3f color;

    public Fog(final float minimalDistance, final float maximalDistance, final Vector3fc color) {
        if (minimalDistance < 0.0f)
            throw new IllegalArgumentException("Minimal distance must be greater than or equal to 0.0f");

        if (maximalDistance <= minimalDistance)
            throw new IllegalArgumentException("Maximal distance must be greater than minimal distance");

        this.minimalDistance = minimalDistance;
        this.maximalDistance = maximalDistance;
        this.color = new Vector3f(color);
    }

    public void setMinimalDistance(final float minimalDistance) {
        this.minimalDistance = Math.max(0, minimalDistance);
    }

    public void setMaximalDistance(final float maximalDistance) {
        this.maximalDistance = Math.max(minimalDistance, maximalDistance);
    }
}