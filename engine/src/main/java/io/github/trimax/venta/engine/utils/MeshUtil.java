package io.github.trimax.venta.engine.utils;

import io.github.trimax.venta.core.model.common.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.joml.Vector3f;

@UtilityClass
public final class MeshUtil {
    public Node<MeshReference> normalize(@NonNull final Node<MeshReference> root) {
        final var boundingBox = GeometryUtil.computeBoundingBox(root);
        final var center = boundingBox.center();
        final var size = boundingBox.size();
        final float scaleFactor = 1.0f / Math.max(size.x, Math.max(size.y, size.z));

        normalizeNode(root, center, scaleFactor);

        return root;
    }

    private void normalizeNode(final Node<MeshReference> node, final Vector3f center, final float scale) {
        final var reference = node.value();
        if (reference != null && reference.hasTransform()) {
            final var transform = reference.transform();
            transform.setPosition(new Vector3f(transform.getPosition()).sub(center).mul(scale));
            transform.setScale(new Vector3f(transform.getScale()).mul(scale));
        }

        if (node.hasChildren())
            for (Node<MeshReference> child : node.children())
                normalizeNode(child, center, scale);
    }
}
