package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;

final class GeometryUtilTest {
    private static final int FLOATS_PER_VERTEX = 5;

    @Test
    void testGridSizes() {
        final var grid = GeometryUtil.createGrid(new Vector2i(10, 20), new Vector2i(2, 3));

        assertEquals(3 * 4, grid.verticesCount(), "Vertices count must be (segments + 1) per axis");
        assertEquals(3 * 4 * FLOATS_PER_VERTEX, grid.vertices().length, "Each vertex has a position and texture coordinates");
        assertEquals(2 * 3 * 6, grid.facets().length, "Each segment has two triangles");
    }

    @Test
    void testGridIsCenteredAndCoversTextureSpace() {
        final var grid = GeometryUtil.createGrid(new Vector2i(10, 20), new Vector2i(2, 3));
        final var vertices = grid.vertices();

        for (int i = 0; i < grid.verticesCount(); i++) {
            final var offset = i * FLOATS_PER_VERTEX;
            assertTrue(Math.abs(vertices[offset]) <= 5.f, "x must be within [-5, 5]");
            assertEquals(0.f, vertices[offset + 1], "Grid must be flat");
            assertTrue(Math.abs(vertices[offset + 2]) <= 10.f, "z must be within [-10, 10]");
            assertTrue(vertices[offset + 3] >= 0.f && vertices[offset + 3] <= 1.f, "u must be within [0, 1]");
            assertTrue(vertices[offset + 4] >= 0.f && vertices[offset + 4] <= 1.f, "v must be within [0, 1]");
        }

        final var last = (grid.verticesCount() - 1) * FLOATS_PER_VERTEX;
        assertEquals(-5.f, vertices[0], "First vertex x");
        assertEquals(-10.f, vertices[2], "First vertex z");
        assertEquals(5.f, vertices[last], "Last vertex x");
        assertEquals(10.f, vertices[last + 2], "Last vertex z");
    }

    @Test
    void testGridTrianglesFaceUp() {
        final var grid = GeometryUtil.createGrid(new Vector2i(4, 4), new Vector2i(2, 2));

        for (int facet = 0; facet < grid.facets().length; facet += 3) {
            final var a = position(grid.vertices(), grid.facets()[facet]);
            final var b = position(grid.vertices(), grid.facets()[facet + 1]);
            final var c = position(grid.vertices(), grid.facets()[facet + 2]);

            final var normal = b.sub(a, new Vector3f()).cross(c.sub(a, new Vector3f()));
            assertTrue(normal.y > 0, "Triangle " + facet / 3 + " must be counter-clockwise when seen from above");
        }
    }

    @Test
    void testInvalidGridIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> GeometryUtil.createGrid(new Vector2i(10, 10), new Vector2i(0, 5)), "Zero segments");
        assertThrows(IllegalArgumentException.class, () -> GeometryUtil.createGrid(new Vector2i(10, 10), new Vector2i(5, -1)), "Negative segments");
        assertThrows(IllegalArgumentException.class, () -> GeometryUtil.createGrid(new Vector2i(0, 10), new Vector2i(5, 5)), "Zero size");
    }

    @Test
    void testBoundingBoxAppliesNodeTransforms() {
        final var child = new Transform();
        child.setPosition(new Vector3f(5, 0, 0));
        child.setScale(new Vector3f(2));
        final var root = new Node<>("root", new MeshReference(TestMeshes.cube(1), null, null),
                List.of(new Node<>("child", new MeshReference(TestMeshes.cube(1), null, child), null)));

        final var box = GeometryUtil.computeBoundingBox(root);

        assertEquals(new Vector3f(-0.5f, -1, -1), box.min(), "Minimum must include the root mesh and the scaled child");
        assertEquals(new Vector3f(6, 1, 1), box.max(), "Maximum must include the moved and scaled child");
    }

    private static Vector3f position(final float[] vertices, final int index) {
        final var offset = index * FLOATS_PER_VERTEX;
        return new Vector3f(vertices[offset], vertices[offset + 1], vertices[offset + 2]);
    }
}
