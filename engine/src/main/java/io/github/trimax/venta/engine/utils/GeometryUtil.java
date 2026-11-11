package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
        if (segments.x() < 1 || segments.y() < 1)
            throw new IllegalArgumentException("Grid must have at least one segment in each direction, got " + segments);

        if (size.x() <= 0 || size.y() <= 0)
            throw new IllegalArgumentException("Grid size must be positive, got " + size);

        final var vertexCountX = segments.x() + 1;
        final var vertexCountZ = segments.y() + 1;

        final var vertexCount = vertexCountX * vertexCountZ;
        final var vertices = new float[vertexCount * 5];
        final var facets = new int[segments.x() * segments.y() * 6];

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

                facets[facetIndex++] = topLeft;
                facets[facetIndex++] = bottomLeft;
                facets[facetIndex++] = topRight;

                facets[facetIndex++] = topRight;
                facets[facetIndex++] = bottomLeft;
                facets[facetIndex++] = bottomRight;
            }
        }

        return new Grid(vertices, facets, vertexCount, facets.length);
    }

    /** Bounding box of a mesh hierarchy, with node transforms applied as the renderer does */
    public BoundingBox computeBoundingBox(@NonNull final Node<MeshReference> node) {
        return computeBoundingBox(node, true);
    }

    /** Bounding box of a mesh hierarchy; the transform of the root itself is applied only if requested */
    BoundingBox computeBoundingBox(@NonNull final Node<MeshReference> node, final boolean includeRootTransform) {
        final var min = new Vector3f(Float.POSITIVE_INFINITY);
        final var max = new Vector3f(Float.NEGATIVE_INFINITY);

        collectBounds(node, new Matrix4f(), includeRootTransform, min, max);

        return new BoundingBox(min, max);
    }

    private void collectBounds(final Node<MeshReference> node, final Matrix4fc parentMatrix, final boolean applyTransform,
                               final Vector3f min, final Vector3f max) {
        final var matrix = new Matrix4f(parentMatrix);
        final var reference = node.value();
        if (applyTransform && reference != null && reference.hasTransform())
            matrix.mul(reference.transform().getMatrix());

        if (reference != null && reference.hasMesh()) {
            final var box = reference.mesh().getBoundingBox();
            final var corner = new Vector3f();
            for (int i = 0; i < 8; i++) {
                corner.set((i & 1) == 0 ? box.min().x : box.max().x,
                        (i & 2) == 0 ? box.min().y : box.max().y,
                        (i & 4) == 0 ? box.min().z : box.max().z);
                matrix.transformPosition(corner);
                min.min(corner);
                max.max(corner);
            }
        }

        if (node.hasChildren())
            for (final var child : node.children())
                collectBounds(child, matrix, true, min, max);
    }
}
