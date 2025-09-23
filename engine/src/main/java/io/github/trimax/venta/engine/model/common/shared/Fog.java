package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.dto.scene.SceneFogDTO;
import lombok.Data;
import lombok.NonNull;

@Data
public final class Fog {
    private float minimalDistance;
    private float maximalDistance;

    @NonNull
    private Vector3f color;

    public Fog(@NonNull final SceneFogDTO dto) {
        this(dto.distance().min(), dto.distance().max(), dto.color().toVector3f());
    }

    public Fog(final float minimalDistance, final float maximalDistance, @NonNull final Vector3fc color) {
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