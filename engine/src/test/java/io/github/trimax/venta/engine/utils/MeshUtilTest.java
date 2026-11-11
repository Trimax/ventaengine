package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;

final class MeshUtilTest {
    private static final float DELTA = 1e-4f;

    @Test
    void testSingleMeshIsCenteredAndScaledToUnit() {
        final var root = node(TestMeshes.mesh(new Vector3f(2, 0, 0), new Vector3f(6, 2, 1)), transform(new Vector3f(), new Vector3f(1)));

        final var box = GeometryUtil.computeBoundingBox(MeshUtil.normalize(root));

        assertBox(box, new Vector3f(-0.5f, -0.25f, -0.125f), new Vector3f(0.5f, 0.25f, 0.125f));
    }

    @Test
    void testHierarchyUsesChildTransforms() {
        // Two unit cubes at x = -5 and x = 5: the object is 11 units wide
        final var root = new Node<>("root", new MeshReference(null, null, new Transform()), List.of(
                node(TestMeshes.cube(1), transform(new Vector3f(-5, 0, 0), new Vector3f(1))),
                node(TestMeshes.cube(1), transform(new Vector3f(5, 0, 0), new Vector3f(1)))));

        final var box = GeometryUtil.computeBoundingBox(MeshUtil.normalize(root));

        assertBox(box, new Vector3f(-0.5f, -0.5f / 11, -0.5f / 11), new Vector3f(0.5f, 0.5f / 11, 0.5f / 11));
    }

    @Test
    void testNestedChildrenAreScaledOnce() {
        // Mesh two levels deep under a scaled parent
        final var leaf = node(TestMeshes.cube(1), transform(new Vector3f(1, 0, 0), new Vector3f(1)));
        final var middle = new Node<>("middle", new MeshReference(null, null, transform(new Vector3f(), new Vector3f(4))), List.of(leaf));
        final var root = new Node<>("root", new MeshReference(null, null, new Transform()), List.of(middle));

        final var box = GeometryUtil.computeBoundingBox(MeshUtil.normalize(root));

        assertBox(box, new Vector3f(-0.5f), new Vector3f(0.5f));
    }

    @Test
    void testRootTransformIsAppliedOnTopOfNormalization() {
        final var root = node(TestMeshes.cube(10), transform(new Vector3f(3, 0, 0), new Vector3f(2)));

        final var box = GeometryUtil.computeBoundingBox(MeshUtil.normalize(root));

        assertBox(box, new Vector3f(2, -1, -1), new Vector3f(4, 1, 1));
    }

    @Test
    void testRootWithoutTransformGetsOne() {
        final var root = new Node<>("root", new MeshReference(TestMeshes.mesh(new Vector3f(0), new Vector3f(4)), null, null), null);

        final var normalized = MeshUtil.normalize(root);

        assertTrue(normalized.value().hasTransform(), "Normalization must be stored in a root transform");
        assertBox(GeometryUtil.computeBoundingBox(normalized), new Vector3f(-0.5f), new Vector3f(0.5f));
    }

    @Test
    void testHierarchyWithoutMeshesIsNotChanged() {
        final var transform = transform(new Vector3f(1, 2, 3), new Vector3f(1));
        final var root = new Node<>("root", new MeshReference(null, null, transform), null);

        MeshUtil.normalize(root);

        assertEquals(new Vector3f(1, 2, 3), transform.getPosition(), "Position must not become NaN or change");
        assertEquals(new Vector3f(1), transform.getScale(), "Scale must not become NaN or change");
    }

    private static Node<MeshReference> node(final MeshEntityImplementation mesh, final Transform transform) {
        return new Node<>("node", new MeshReference(mesh, null, transform), null);
    }

    private static Transform transform(final Vector3fc position, final Vector3fc scale) {
        final var transform = new Transform();
        transform.setPosition(position);
        transform.setScale(scale);
        return transform;
    }

    private static void assertBox(final BoundingBox box, final Vector3f min, final Vector3f max) {
        assertEquals(min.x, box.min().x, DELTA, "min x");
        assertEquals(min.y, box.min().y, DELTA, "min y");
        assertEquals(min.z, box.min().z, DELTA, "min z");
        assertEquals(max.x, box.max().x, DELTA, "max x");
        assertEquals(max.y, box.max().y, DELTA, "max y");
        assertEquals(max.z, box.max().z, DELTA, "max z");
    }
}
