package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.terrain.Heightmap;
import io.github.trimax.venta.engine.model.common.terrain.TerrainChunkLayout;

final class TerrainChunkUtilTest {
    private static final Vector2f SIZE = new Vector2f(80.f, 80.f);

    private static Heightmap flat(final float value) {
        final var values = new float[16];
        java.util.Arrays.fill(values, value);
        return new Heightmap(4, 4, values);
    }

    /** Rises from 0 on the left to 1 on the right, the rows are the same */
    private static Heightmap ramp() {
        final var values = new float[64];
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++)
                values[y * 8 + x] = x / 7.f;

        return new Heightmap(8, 8, values);
    }

    private static TerrainChunkLayout build(final int segmentsX, final int segmentsZ) {
        return TerrainChunkUtil.build(SIZE, new Vector2i(segmentsX, segmentsZ), flat(0.5f));
    }

    @Test
    void testGridOfAMultipleOfTheChunkIsCutIntoChunksWithLevels() {
        final var layout = build(64, 64);

        assertEquals(32, layout.getChunkCellsX());
        assertEquals(2, layout.getChunksX());
        assertEquals(2, layout.getChunksZ());
        assertEquals(4, layout.getChunkCount());
        assertEquals(5, layout.getLevels(), "The steps of 1, 2, 4, 8 and 16 cells: the coarsest has two cells along a side");
    }

    @Test
    void testChunkIsTheBiggestPowerOfTwoThatDividesTheGrid() {
        final var big = build(256, 256);
        assertEquals(32, big.getChunkCellsX());
        assertEquals(8, big.getChunksX());
        assertEquals(5, big.getLevels());

        final var uneven = build(96, 64);
        assertEquals(32, uneven.getChunkCellsX(), "gcd(96, 64, 32)");
        assertEquals(3, uneven.getChunksX());
        assertEquals(2, uneven.getChunksZ());

        final var small = build(12, 12);
        assertEquals(4, small.getChunkCellsX(), "gcd(12, 12, 32)");
        assertEquals(2, small.getLevels());
    }

    @Test
    void testGridThatHasNoSuchDivisorIsOneChunkWithOneLevel() {
        final var layout = build(10, 6);

        assertEquals(1, layout.getChunkCount());
        assertEquals(10, layout.getChunkCellsX());
        assertEquals(6, layout.getChunkCellsZ());
        assertEquals(1, layout.getLevels());
        assertEquals(10 * 6 * 2 * 3 + 2 * (10 + 6) * 6, layout.getCount(0, 0), "All the quads of the surface and the skirts around");
    }

    @Test
    void testBadGridIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> build(0, 8));
        assertThrows(IllegalArgumentException.class, () -> build(8, -1));
    }

    @Test
    void testVerticesAreTheGridAndTheSkirtsOfTheBorders() {
        final var layout = build(64, 64);

        // 65 by 65 vertices of the grid; the ones with a coordinate that is a multiple of 32 (381) have a skirt vertex too
        assertEquals((4225 + 381) * 5, layout.getVertices().length);
    }

    @Test
    void testSkirtVerticesRepeatTheBorderOnesFlagged() {
        final var layout = build(64, 64);
        final var vertices = layout.getVertices();

        var skirts = 0;
        for (int index = 4225; index < vertices.length / 5; index++) {
            assertEquals(TerrainChunkLayout.SKIRT_FLAG, vertices[index * 5 + 1], "The flag of a skirt vertex");
            skirts++;
        }

        assertEquals(381, skirts);

        // The first border vertex of the grid is (0, 0), and its skirt is the first one
        assertEquals(vertices[0], vertices[4225 * 5], "x");
        assertEquals(vertices[2], vertices[4225 * 5 + 2], "z");
        assertEquals(vertices[3], vertices[4225 * 5 + 3], "u");
        assertEquals(vertices[4], vertices[4225 * 5 + 4], "v");
    }

    @Test
    void testGridVerticesAreOnTheGridWithZeroHeight() {
        final var layout = build(64, 64);
        final var vertices = layout.getVertices();

        assertEquals(-40.f, vertices[0], 1e-4f, "The grid is centered");
        assertEquals(0.f, vertices[1]);
        assertEquals(40.f, vertices[(65 * 65 - 1) * 5], 1e-4f);
        assertEquals(1.f, vertices[(65 * 65 - 1) * 5 + 3], 1e-6f, "u at the end");
    }

    @Test
    void testIndexRangesOfAllChunksAndLevelsAreTightAndInTheBuffer() {
        final var layout = build(64, 64);

        // Level 0 of a chunk: 32 by 32 quads and 128 skirt quads (32 per side) of 6 indices; level 1: 16 by 16 quads and 64 skirt quads
        assertEquals((1024 + 128) * 6, layout.getCount(0, 0));
        assertEquals((256 + 64) * 6, layout.getCount(0, 1));

        final Set<Integer> covered = new HashSet<>();
        var total = 0;
        for (int chunk = 0; chunk < layout.getChunkCount(); chunk++)
            for (int level = 0; level < layout.getLevels(); level++) {
                final var from = layout.getOffset(chunk, level);
                final var count = layout.getCount(chunk, level);

                assertTrue(from >= 0 && from + count <= layout.getIndices().length, "In the buffer");
                for (int i = from; i < from + count; i++)
                    assertTrue(covered.add(i), "No index belongs to two ranges");

                total += count;
            }

        assertEquals(layout.getIndices().length, total, "No index is left out of the ranges");
    }

    @Test
    void testEveryIndexIsAVertexAndEveryTriangleHasThreeDistinctOnes() {
        final var layout = build(64, 64);
        final var vertexCount = layout.getVertices().length / 5;

        for (final var index : layout.getIndices())
            assertTrue(index >= 0 && index < vertexCount, "Vertex " + index);

        for (int i = 0; i < layout.getIndices().length; i += 3) {
            final var a = layout.getIndices()[i];
            final var b = layout.getIndices()[i + 1];
            final var c = layout.getIndices()[i + 2];
            assertTrue(a != b && b != c && a != c, "A triangle has three vertices: " + a + ", " + b + ", " + c);
        }
    }

    /** The triangles of the surface (no skirts) of the level 0 of all the chunks, each as a sorted triple */
    private static Set<String> surface(final TerrainChunkLayout layout, final int level) {
        final var gridVertices = (layout.getSegments().x() + 1) * (layout.getSegments().y() + 1);
        final Set<String> triangles = new HashSet<>();

        for (int chunk = 0; chunk < layout.getChunkCount(); chunk++) {
            final var from = layout.getOffset(chunk, level);
            for (int i = from; i < from + layout.getCount(chunk, level); i += 3) {
                final var triangle = new int[]{layout.getIndices()[i], layout.getIndices()[i + 1], layout.getIndices()[i + 2]};
                if (triangle[0] >= gridVertices || triangle[1] >= gridVertices || triangle[2] >= gridVertices)
                    continue;

                java.util.Arrays.sort(triangle);
                triangles.add(triangle[0] + "," + triangle[1] + "," + triangle[2]);
            }
        }

        return triangles;
    }

    @Test
    void testLevelZeroHasTheTrianglesOfTheWholeGrid() {
        final var layout = build(64, 64);
        final var grid = GeometryUtil.createGrid(new Vector2i(80, 80), new Vector2i(64, 64));

        final Set<String> expected = new HashSet<>();
        for (int i = 0; i < grid.facets().length; i += 3) {
            final var triangle = new int[]{grid.facets()[i], grid.facets()[i + 1], grid.facets()[i + 2]};
            java.util.Arrays.sort(triangle);
            expected.add(triangle[0] + "," + triangle[1] + "," + triangle[2]);
        }

        assertEquals(expected, surface(layout, 0), "The chunks together are the grid, with nothing missing and nothing twice");
        assertEquals(8192, surface(layout, 0).size());
    }

    @Test
    void testCoarserLevelHasFewerTrianglesOnTheSameSurface() {
        final var layout = build(64, 64);

        assertEquals(2048, surface(layout, 1).size(), "Every quad of 2 cells is two triangles: 4 chunks of 16 by 16 quads");
        assertTrue(surface(layout, 1).size() < surface(layout, 0).size());
    }

    @Test
    void testCoarseLevelUsesOnlyEveryOtherVertexOfTheGrid() {
        final var layout = build(64, 64);

        for (int chunk = 0; chunk < layout.getChunkCount(); chunk++) {
            final var from = layout.getOffset(chunk, 1);
            for (int i = from; i < from + layout.getCount(chunk, 1); i++) {
                final var index = layout.getIndices()[i];
                if (index >= 4225)
                    continue;

                assertEquals(0, index % 65 % 2, "The column of vertex " + index + " is even");
                assertEquals(0, index / 65 % 2, "The row of vertex " + index + " is even");
            }
        }
    }

    @Test
    void testSkirtsHangFromTheEdgesOfEveryLevelAndTouchOnlyTheBorder() {
        final var layout = build(64, 64);
        final var vertices = layout.getVertices();

        for (int level = 0; level < layout.getLevels(); level++)
            for (int chunk = 0; chunk < layout.getChunkCount(); chunk++) {
                final var from = layout.getOffset(chunk, level);
                for (int i = from; i < from + layout.getCount(chunk, level); i += 3)
                    for (int k = 0; k < 3; k++) {
                        final var index = layout.getIndices()[i + k];
                        if (index < 4225)
                            continue;

                        final var x = Math.round((vertices[index * 5 + 3]) * 64);
                        final var z = Math.round((vertices[index * 5 + 4]) * 64);
                        assertTrue(x % 32 == 0 || z % 32 == 0, "A skirt vertex is on the border of a chunk: " + x + ", " + z);
                    }
            }
    }

    @Test
    void testRangeOfHeightsOfFlatTerrain() {
        final var layout = TerrainChunkUtil.build(SIZE, new Vector2i(64, 64), flat(0.3f));

        for (int chunk = 0; chunk < layout.getChunkCount(); chunk++) {
            assertEquals(0.3f, layout.getMinimum(chunk), 1e-5f);
            assertEquals(0.3f, layout.getMaximum(chunk), 1e-5f);
        }
    }

    @Test
    void testRangeOfHeightsFollowsTheHeightmap() {
        final var layout = TerrainChunkUtil.build(SIZE, new Vector2i(64, 64), ramp());

        // Chunks 0 and 2 are on the left, 1 and 3 on the right of the ramp
        assertTrue(layout.getMaximum(0) < layout.getMinimum(1) + 0.2f, "The right is higher");
        assertTrue(layout.getMinimum(1) > layout.getMinimum(0));
        assertTrue(layout.getMaximum(1) > layout.getMaximum(0));
        assertTrue(layout.getMaximum(0) > layout.getMinimum(0), "A chunk on a ramp has a range");
        assertEquals(layout.getMinimum(0), layout.getMinimum(2), 1e-5f, "The rows of the ramp are the same");
    }

    @Test
    void testChunkSizesAreTheShareOfTheGrid() {
        final var layout = build(64, 64);

        assertEquals(40.f, layout.getChunkSizeX(), 1e-4f);
        assertEquals(40.f, layout.getChunkSizeZ(), 1e-4f);
    }
}
