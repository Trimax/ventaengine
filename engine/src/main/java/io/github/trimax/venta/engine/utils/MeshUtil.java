package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MeshUtil {
    /**
     * Centers the content of the hierarchy at the origin and scales it to fit a unit cube.
     * The transform of the root (position, rotation, scale from the object file) is applied on top of the normalization.
     * Child transforms are relative to their parents, so only the root transform is changed.
     */
    public Node<MeshReference> normalize(@NonNull final Node<MeshReference> root) {
        final var boundingBox = GeometryUtil.computeBoundingBox(root, false);
        final var size = boundingBox.size();
        final var maximalSize = Math.max(size.x, Math.max(size.y, size.z));
        if (!Float.isFinite(maximalSize) || maximalSize <= 0.f)
            return root;

        final var center = boundingBox.center();
        final var scale = 1.0f / maximalSize;

        final var reference = root.value();
        if (reference != null && reference.hasTransform()) {
            normalize(reference.transform(), center, scale);
            return root;
        }

        final var transform = new Transform();
        normalize(transform, center, scale);

        return new Node<>(root.name(),
                new MeshReference(reference != null ? reference.mesh() : null, reference != null ? reference.material() : null, transform),
                root.children());
    }

    /** Replaces the transform M with M * S(scale) * T(-center), decomposed back to position, rotation and scale */
    private void normalize(final Transform transform, final Vector3f center, final float scale) {
        final var newScale = new Vector3f(transform.getScale()).mul(scale);
        final var offset = new Vector3f(center).mul(newScale);
        new Matrix4f().rotateXYZ(transform.getRotation()).transformDirection(offset);

        transform.setPosition(new Vector3f(transform.getPosition()).sub(offset));
        transform.setScale(newScale);
    }
}
