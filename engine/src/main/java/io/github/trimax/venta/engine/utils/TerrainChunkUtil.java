package io.github.trimax.venta.engine.utils;

import org.joml.Vector2fc;
import org.joml.Vector2ic;

import io.github.trimax.venta.engine.model.common.terrain.Heightmap;
import io.github.trimax.venta.engine.model.common.terrain.TerrainChunkLayout;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Cuts the grid of a terrain into chunks (see {@link TerrainChunkLayout}) and makes the indices of their levels of detail.
 */
@UtilityClass
public final class TerrainChunkUtil {
    /** A chunk of fewer cells is not worth a draw call: the grid that has no such divisor is one chunk */
    private static final int MINIMAL_CHUNK_CELLS = 4;

    /**
     * Cuts a grid into chunks. The chunk is the biggest power of two of cells (up to {@link TerrainChunkLayout#MAXIMAL_CHUNK_CELLS})
     * that divides the segments of the grid; a grid that is not a multiple of {@value #MINIMAL_CHUNK_CELLS} cells is one chunk
     * with one level.
     *
     * @param size     the size of the grid in the local space
     * @param segments the cells of the grid along x and along z
     * @param heights  the heightmap that displaces the grid: the range of the heights of a chunk comes from it
     */
    public TerrainChunkLayout build(@NonNull final Vector2fc size, @NonNull final Vector2ic segments, @NonNull final Heightmap heights) {
        if (segments.x() < 1 || segments.y() < 1)
            throw new IllegalArgumentException("Grid must have at least one segment in each direction, got " + segments);

        final var divisor = greatestCommonDivisor(greatestCommonDivisor(segments.x(), segments.y()), TerrainChunkLayout.MAXIMAL_CHUNK_CELLS);
        final var split = divisor >= MINIMAL_CHUNK_CELLS;

        final var cellsX = split ? divisor : segments.x();
        final var cellsZ = split ? divisor : segments.y();
        final var levels = split ? Integer.numberOfTrailingZeros(divisor) : 1;

        final var chunksX = segments.x() / cellsX;
        final var chunksZ = segments.y() / cellsZ;
        final var chunkCount = chunksX * chunksZ;

        final var columns = segments.x() + 1;
        final var rows = segments.y() + 1;

        // The skirts hang from the vertices of the borders of the chunks
        final var skirtOf = new int[columns * rows];
        var vertexCount = columns * rows;
        for (int z = 0; z < rows; z++)
            for (int x = 0; x < columns; x++)
                skirtOf[z * columns + x] = x % cellsX == 0 || z % cellsZ == 0 ? vertexCount++ : -1;

        final var vertices = new float[vertexCount * 5];
        for (int z = 0; z < rows; z++)
            for (int x = 0; x < columns; x++) {
                final var u = (float) x / segments.x();
                final var v = (float) z / segments.y();
                final var x0 = (u - 0.5f) * size.x();
                final var z0 = (v - 0.5f) * size.y();

                put(vertices, z * columns + x, x0, 0.f, z0, u, v);
                if (skirtOf[z * columns + x] >= 0)
                    put(vertices, skirtOf[z * columns + x], x0, TerrainChunkLayout.SKIRT_FLAG, z0, u, v);
            }

        // The sizes of the levels are known, so the indices are made in one array
        final var offsets = new int[chunkCount * levels];
        final var counts = new int[chunkCount * levels];
        var total = 0;
        for (int level = 0; level < levels; level++) {
            final var step = 1 << level;
            final var count = countIndices(cellsX / step, cellsZ / step);
            for (int chunk = 0; chunk < chunkCount; chunk++) {
                offsets[chunk * levels + level] = total;
                counts[chunk * levels + level] = count;
                total += count;
            }
        }

        final var indices = new int[total];
        final var minimums = new float[chunkCount];
        final var maximums = new float[chunkCount];
        for (int row = 0; row < chunksZ; row++)
            for (int column = 0; column < chunksX; column++) {
                final var chunk = row * chunksX + column;
                for (int level = 0; level < levels; level++)
                    fill(indices, offsets[chunk * levels + level], column * cellsX, row * cellsZ, cellsX, cellsZ, 1 << level, columns, skirtOf);

                measure(heights, segments, column * cellsX, row * cellsZ, cellsX, cellsZ, chunk, minimums, maximums);
            }

        return new TerrainChunkLayout(size, segments, cellsX, cellsZ, levels, vertices, indices, offsets, counts, minimums, maximums);
    }

    /** The indices of a chunk of cells at a step: two triangles for every quad of the surface and two for every quad of the skirts */
    private int countIndices(final int quadsX, final int quadsZ) {
        return (quadsX * quadsZ + 2 * quadsX + 2 * quadsZ) * 6;
    }

    private void put(final float[] vertices, final int index, final float x, final float y, final float z, final float u, final float v) {
        vertices[index * 5] = x;
        vertices[index * 5 + 1] = y;
        vertices[index * 5 + 2] = z;
        vertices[index * 5 + 3] = u;
        vertices[index * 5 + 4] = v;
    }

    private void fill(final int[] indices, final int start, final int x0, final int z0, final int cellsX, final int cellsZ,
                      final int step, final int columns, final int[] skirtOf) {
        var position = start;

        // The surface: the quads are split like the ones of the whole grid (see GeometryUtil.createGrid)
        for (int z = z0; z < z0 + cellsZ; z += step)
            for (int x = x0; x < x0 + cellsX; x += step) {
                final var topLeft = z * columns + x;
                final var topRight = topLeft + step;
                final var bottomLeft = (z + step) * columns + x;
                final var bottomRight = bottomLeft + step;

                indices[position++] = topLeft;
                indices[position++] = bottomLeft;
                indices[position++] = topRight;

                indices[position++] = topRight;
                indices[position++] = bottomLeft;
                indices[position++] = bottomRight;
            }

        // The skirts along the four borders, between the vertices of the level
        for (int x = x0; x < x0 + cellsX; x += step) {
            position = skirt(indices, position, z0 * columns + x, z0 * columns + x + step, skirtOf);
            position = skirt(indices, position, (z0 + cellsZ) * columns + x + step, (z0 + cellsZ) * columns + x, skirtOf);
        }

        for (int z = z0; z < z0 + cellsZ; z += step) {
            position = skirt(indices, position, (z + step) * columns + x0, z * columns + x0, skirtOf);
            position = skirt(indices, position, z * columns + x0 + cellsX, (z + step) * columns + x0 + cellsX, skirtOf);
        }
    }

    /** A quad of a skirt under the edge from a vertex to the next one */
    private int skirt(final int[] indices, final int start, final int from, final int to, final int[] skirtOf) {
        var position = start;

        indices[position++] = from;
        indices[position++] = skirtOf[from];
        indices[position++] = to;

        indices[position++] = to;
        indices[position++] = skirtOf[from];
        indices[position++] = skirtOf[to];

        return position;
    }

    /** The range of the heights of a chunk: the heightmap at all the vertices of the grid in it (the coarse levels use some of them) */
    private void measure(final Heightmap heights, final Vector2ic segments, final int x0, final int z0, final int cellsX, final int cellsZ,
                         final int chunk, final float[] minimums, final float[] maximums) {
        var minimum = Float.POSITIVE_INFINITY;
        var maximum = Float.NEGATIVE_INFINITY;

        for (int z = z0; z <= z0 + cellsZ; z++)
            for (int x = x0; x <= x0 + cellsX; x++) {
                final var value = heights.sample((float) x / segments.x(), (float) z / segments.y());
                minimum = Math.min(minimum, value);
                maximum = Math.max(maximum, value);
            }

        minimums[chunk] = minimum;
        maximums[chunk] = maximum;
    }

    private int greatestCommonDivisor(final int a, final int b) {
        return b == 0 ? a : greatestCommonDivisor(b, a % b);
    }
}
