package io.github.trimax.venta.engine.model.geo;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.MeshDTO;

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
}
