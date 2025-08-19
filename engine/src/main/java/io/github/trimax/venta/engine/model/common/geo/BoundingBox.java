package io.github.trimax.venta.engine.model.common.geo;

import io.github.trimax.venta.engine.model.dto.MeshDTO;
import org.joml.Vector3f;

public record BoundingBox(Vector3f min, Vector3f max) {
    public static BoundingBox of(final MeshDTO mesh) {
        final var min = new Vector3f(Float.POSITIVE_INFINITY);
        final var max = new Vector3f(Float.NEGATIVE_INFINITY);
        for (final MeshDTO.Vertex v : mesh.vertices()) {
            min.min(v.position());
            max.max(v.position());
        }

        return new BoundingBox(min, max);
    }

    public static BoundingBox of(final BoundingBox... boxes) {
        final var min = new Vector3f(Float.POSITIVE_INFINITY);
        final var max = new Vector3f(Float.NEGATIVE_INFINITY);
        for (final BoundingBox box : boxes) {
            min.min(box.min);
            max.max(box.max);
        }

        return new BoundingBox(min, max);
    }

    public Vector3f size() {
        return new Vector3f(
                this.max().x - this.min().x,
                this.max().y - this.min().y,
                this.max().z - this.min().z);
    }

    public Vector3f center() {
        return new Vector3f(
                (this.min().x + this.max().x) * 0.5f,
                (this.min().y + this.max().y) * 0.5f,
                (this.min().z + this.max().z) * 0.5f);
    }
}
