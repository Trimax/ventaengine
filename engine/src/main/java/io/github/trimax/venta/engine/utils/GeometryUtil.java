package io.github.trimax.venta.engine.utils;

import io.github.trimax.venta.core.model.common.Node;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import lombok.experimental.UtilityClass;
import org.joml.Vector3f;

@UtilityClass
public final class GeometryUtil {
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
