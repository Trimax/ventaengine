package io.github.trimax.venta.engine.model.common.terrain;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

final class HeightmapTest {
    private static final float EPSILON = 1e-5f;

    // 2x2 texels: 0.0 0.2
    //             0.4 0.6
    private static final Heightmap HEIGHTMAP = new Heightmap(2, 2, new float[]{0.f, 0.2f, 0.4f, 0.6f});

    @Test
    void testTexelCentersReturnTexels() {
        assertEquals(0.f, HEIGHTMAP.sample(0.25f, 0.25f), EPSILON);
        assertEquals(0.2f, HEIGHTMAP.sample(0.75f, 0.25f), EPSILON);
        assertEquals(0.4f, HEIGHTMAP.sample(0.25f, 0.75f), EPSILON);
        assertEquals(0.6f, HEIGHTMAP.sample(0.75f, 0.75f), EPSILON);
    }

    @Test
    void testValuesBetweenTexelCentersAreInterpolated() {
        assertEquals(0.1f, HEIGHTMAP.sample(0.5f, 0.25f), EPSILON, "Halfway between two texels of a row");
        assertEquals(0.3f, HEIGHTMAP.sample(0.5f, 0.5f), EPSILON, "Middle of four texels");
    }

    @Test
    void testEdgesWrapAroundLikeRepeat() {
        assertEquals(0.1f, HEIGHTMAP.sample(0.f, 0.25f), EPSILON, "The left edge blends the first and the last column");
        assertEquals(HEIGHTMAP.sample(0.3f, 0.6f), HEIGHTMAP.sample(1.3f, -0.4f), EPSILON, "Coordinates repeat with period 1");
    }

    @Test
    void testGridVerticesSampleHeightmap() {
        final var size = new Vector2f(10.f, 20.f);
        final var segments = new Vector2i(2, 4);

        assertEquals(HEIGHTMAP.sample(0.f, 0.f), HEIGHTMAP.sample(size, segments, -5.f, -10.f), EPSILON, "Corner vertex");
        assertEquals(HEIGHTMAP.sample(0.5f, 0.5f), HEIGHTMAP.sample(size, segments, 0.f, 0.f), EPSILON, "Center vertex");
        assertEquals(HEIGHTMAP.sample(1.f, 0.25f), HEIGHTMAP.sample(size, segments, 5.f, -5.f), EPSILON, "Edge vertex");
    }

    @Test
    void testFlatHeightmapGivesFlatSurface() {
        final var heightmap = new Heightmap(1, 1, new float[]{0.5f});

        assertEquals(0.5f, heightmap.sample(new Vector2f(4.f), new Vector2i(3), 0.7f, -1.3f), EPSILON);
    }

    @Test
    void testCellIsInterpolatedOverItsTriangles() {
        // A single high texel: only the vertex at (-1, -1) is above zero, the other vertices of its upper left cell are zero
        final var values = new float[16];
        values[5] = 1.f;

        final var heightmap = new Heightmap(4, 4, values);
        final var size = new Vector2f(4.f, 4.f);
        final var segments = new Vector2i(4, 4);

        assertEquals(0.25f, heightmap.sample(size, segments, -1.f, -1.f), EPSILON, "Bottom right vertex of the cell");
        assertEquals(0.f, heightmap.sample(size, segments, -1.5f, -1.5f), EPSILON,
                "The cell middle lies on the diagonal between the top right and the bottom left vertices");
        assertEquals(0.f, heightmap.sample(size, segments, -1.75f, -1.75f), EPSILON, "The upper left triangle has zero vertices only");
        assertEquals(0.125f, heightmap.sample(size, segments, -1.25f, -1.25f), EPSILON,
                "The lower right triangle is planar (bilinear interpolation would give 0.140625)");
    }

    @Test
    void testPointsOutsideGridGetNearestEdge() {
        final var size = new Vector2f(10.f, 10.f);
        final var segments = new Vector2i(2, 2);

        assertEquals(HEIGHTMAP.sample(size, segments, 5.f, 1.f), HEIGHTMAP.sample(size, segments, 50.f, 1.f), EPSILON);
        assertEquals(HEIGHTMAP.sample(size, segments, -5.f, -5.f), HEIGHTMAP.sample(size, segments, -7.f, -9.f), EPSILON);
    }

    @Test
    void testInvalidSizesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Heightmap(0, 1, new float[0]));
        assertThrows(IllegalArgumentException.class, () -> new Heightmap(2, 2, new float[3]));
    }
}
