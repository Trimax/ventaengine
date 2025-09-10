package io.github.trimax.venta.engine.utils;

import org.joml.Vector2ic;
import org.joml.Vector3f;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Grid;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class GeometryUtil {
    public Grid createGrid(@NonNull final Vector2ic size, @NonNull final Vector2ic segments) {
        final var vertexCountX = segments.x() + 1;
        final var vertexCountZ = segments.y() + 1;

        final var vertexCount = vertexCountX * vertexCountZ;
        final var vertices = new float[vertexCount * 5];
        final var indices = new int[segments.x() * segments.y() * 6];

        int vertexIndex = 0;
        for (int z = 0; z < vertexCountZ; z++) {
            for (int x = 0; x < vertexCountX; x++) {
                // position
                vertices[vertexIndex++] = ((float)x / segments.x() - 0.5f) * size.x();
                vertices[vertexIndex++] = 0.f;
                vertices[vertexIndex++] = ((float)z / segments.y() - 0.5f) * size.y();

                // texture coordinates
                vertices[vertexIndex++] = (float)x / segments.x();
                vertices[vertexIndex++] = (float)z / segments.y();
            }
        }

        int facetIndex = 0;
        for (int z = 0; z < segments.y(); z++) {
            for (int x = 0; x < segments.x(); x++) {
                final var topLeft = z * vertexCountX + x;
                final var topRight = topLeft + 1;
                final var bottomLeft = (z + 1) * vertexCountX + x;
                final var bottomRight = bottomLeft + 1;

                indices[facetIndex++] = topLeft;
                indices[facetIndex++] = bottomLeft;
                indices[facetIndex++] = topRight;

                indices[facetIndex++] = topRight;
                indices[facetIndex++] = bottomLeft;
                indices[facetIndex++] = bottomRight;
            }
        }

        return new Grid(vertices, indices, vertexCount, indices.length);
    }

    public BoundingBox computeBoundingBox(final Node<MeshReference> node) {
        final var min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        final var max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        collectBounds(node, min, max);

        return new BoundingBox(min, max);
    }

    private void collectBounds(final Node<MeshReference> node, final Vector3f min, final Vector3f max) {
        final var reference = node.value();

        if (reference != null && reference.hasMesh()) {
            final var boundingBox = reference.mesh().getBoundingBox();

            if (boundingBox != null) {
                min.x = Math.min(min.x, boundingBox.min().x);
                min.y = Math.min(min.y, boundingBox.min().y);
                min.z = Math.min(min.z, boundingBox.min().z);

                max.x = Math.max(max.x, boundingBox.max().x);
                max.y = Math.max(max.y, boundingBox.max().y);
                max.z = Math.max(max.z, boundingBox.max().z);
            }
        }

        if (node.hasChildren())
            for (final var child : node.children())
                collectBounds(child, min, max);
    }
}
