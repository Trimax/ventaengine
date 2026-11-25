package io.github.trimax.venta.engine.model.common.terrain;

import org.joml.Vector2fc;
import org.joml.Vector2ic;

import lombok.Getter;
import lombok.NonNull;

/**
 * How a terrain is cut into chunks: the vertices of its grid (and the ones of the skirts), and for every chunk the index ranges
 * of its levels of detail and the range of the heights it covers. It is made once from the grid and the heightmap and doesn't
 * change, so the terrains of one prefab share it (see {@link TerrainChunks} for what changes in a frame).
 *
 * <p>The chunks are squares of {@code chunkCells} cells of the grid. A level of detail takes every second vertex of the one
 * before it: the level n has a step of 2^n cells. Along the border of a chunk a skirt hangs down (the vertices of the skirt are
 * the ones of the border again, flagged with the height -1, which the vertex shader drops by the depth of the skirt), so
 * where the chunks of different levels meet, the crack between them is closed.
 */
public final class TerrainChunkLayout {
    /** The most cells along a side of a chunk */
    public static final int MAXIMAL_CHUNK_CELLS = 32;

    /** The height of the vertices of the skirts that the shaders know them by: the vertices of the grid have 0 */
    public static final float SKIRT_FLAG = -1.f;

    @Getter
    private final Vector2fc size;

    @Getter
    private final Vector2ic segments;

    /** The cells along a side of a chunk, in x and in z */
    @Getter
    private final int chunkCellsX;

    @Getter
    private final int chunkCellsZ;

    @Getter
    private final int chunksX;

    @Getter
    private final int chunksZ;

    /** The levels of detail: the level n has a step of 2^n cells */
    @Getter
    private final int levels;

    /** The positions and the texture coordinates of the vertices: the grid first, then the skirts */
    @Getter
    private final float[] vertices;

    @Getter
    private final int[] indices;

    private final int[] offsets;
    private final int[] counts;
    private final float[] minimums;
    private final float[] maximums;

    public TerrainChunkLayout(@NonNull final Vector2fc size, @NonNull final Vector2ic segments, final int chunkCellsX, final int chunkCellsZ,
                              final int levels, final float @NonNull [] vertices, final int @NonNull [] indices, final int @NonNull [] offsets,
                              final int @NonNull [] counts, final float @NonNull [] minimums, final float @NonNull [] maximums) {
        this.size = size;
        this.segments = segments;
        this.chunkCellsX = chunkCellsX;
        this.chunkCellsZ = chunkCellsZ;
        this.chunksX = segments.x() / chunkCellsX;
        this.chunksZ = segments.y() / chunkCellsZ;
        this.levels = levels;
        this.vertices = vertices;
        this.indices = indices;
        this.offsets = offsets;
        this.counts = counts;
        this.minimums = minimums;
        this.maximums = maximums;
    }

    public int getChunkCount() {
        return chunksX * chunksZ;
    }

    /** The chunk in a column and a row of the chunks (the rows go along z) */
    public int getChunk(final int column, final int row) {
        return row * chunksX + column;
    }

    /** Where the indices of a chunk at a level start in the indices, in indices */
    public int getOffset(final int chunk, final int level) {
        return offsets[chunk * levels + level];
    }

    /** How many indices a chunk has at a level */
    public int getCount(final int chunk, final int level) {
        return counts[chunk * levels + level];
    }

    /** The lowest and the highest value of the heightmap that a chunk covers, from 0 to 1 */
    public float getMinimum(final int chunk) {
        return minimums[chunk];
    }

    public float getMaximum(final int chunk) {
        return maximums[chunk];
    }

    /** The size of a chunk in the local space of the terrain, along x and along z */
    public float getChunkSizeX() {
        return size.x() / chunksX;
    }

    public float getChunkSizeZ() {
        return size.y() / chunksZ;
    }
}
