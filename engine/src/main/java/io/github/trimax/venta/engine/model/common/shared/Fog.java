package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.dto.scene.SceneFogDTO;
import lombok.Data;
import lombok.NonNull;

@Data
public final class Fog {
    /** The shader divides by (maximal - minimal), so the distances are kept at least this far apart */
    private static final float MINIMAL_RANGE = 0.001f;

    private float minimalDistance;
    private float maximalDistance;

    /**
     * Top of the fog layer in world space: only the part of the way below it is fogged.
     * Infinite by default, so the whole way is fogged and the fog hides the whole sky.
     */
    private float height = Float.POSITIVE_INFINITY;

    @NonNull
    private Vector3f color;

    public Fog(@NonNull final SceneFogDTO dto) {
        this(dto.distance().min(), dto.distance().max(), dto.color().toVector3f());

        if (dto.height() != null)
            this.height = dto.height();
    }

    public Fog(final float minimalDistance, final float maximalDistance, final float height, @NonNull final Vector3fc color) {
        this(minimalDistance, maximalDistance, color);
        this.height = height;
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

    /** Sets the minimal distance; the maximal distance is moved forward if it would not be greater anymore */
    public void setMinimalDistance(final float minimalDistance) {
        this.minimalDistance = Math.max(0, minimalDistance);
        this.maximalDistance = Math.max(this.maximalDistance, this.minimalDistance + MINIMAL_RANGE);
    }

    /** Sets the maximal distance; it is kept greater than the minimal distance */
    public void setMaximalDistance(final float maximalDistance) {
        this.maximalDistance = Math.max(maximalDistance, minimalDistance + MINIMAL_RANGE);
    }
}