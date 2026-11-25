package io.github.trimax.venta.engine.model.common.terrain;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import lombok.Getter;
import lombok.NonNull;

/**
 * The chunks of a terrain at work: the level of detail every chunk is drawn with in this frame and the box it fills in the local
 * space (for the culling). The layout and the geometry are shared by the terrains of a prefab, and this is what belongs to one
 * terrain.
 */
public final class TerrainChunks {
    /** The skirts are as deep as this share of the range of the heights, but not less than a share of the size of a chunk */
    private static final float SKIRT_HEIGHT_SHARE = 0.05f;
    private static final float SKIRT_CHUNK_SHARE = 0.03f;

    @Getter
    private final TerrainChunkLayout layout;

    @Getter
    private final Geometry geometry;

    private final int[] levels;
    private final BoundingBox[] boxes;

    /** The range of the heights the boxes are made for, not a number before they are */
    private float boxesFactor = Float.NaN;

    public TerrainChunks(@NonNull final TerrainChunkLayout layout, @NonNull final Geometry geometry) {
        this.layout = layout;
        this.geometry = geometry;
        this.levels = new int[layout.getChunkCount()];
        this.boxes = new BoundingBox[layout.getChunkCount()];

        for (int chunk = 0; chunk < boxes.length; chunk++)
            boxes[chunk] = new BoundingBox(new Vector3f(), new Vector3f());
    }

    /** How deep the skirts hang in the local space of the terrain, for a range of the heights */
    public float getSkirtDepth(final float factor) {
        return Math.max(Math.abs(factor) * SKIRT_HEIGHT_SHARE, Math.max(layout.getChunkSizeX(), layout.getChunkSizeZ()) * SKIRT_CHUNK_SHARE);
    }

    /** The level of detail a chunk is drawn with */
    public int getLevel(final int chunk) {
        return levels[chunk];
    }

    public void setLevel(final int chunk, final int level) {
        levels[chunk] = Math.clamp(level, 0, layout.getLevels() - 1);
    }

    /**
     * The box of a chunk in the local space of the terrain: it holds the heights of the chunk and the skirts under it.
     * The heights depend on the range of the heightmap, so the boxes are made again when it changes.
     */
    public BoundingBox getBox(final int chunk, final float factor) {
        if (Float.compare(boxesFactor, factor) != 0) {
            fillBoxes(factor);
            boxesFactor = factor;
        }

        return boxes[chunk];
    }

    private void fillBoxes(final float factor) {
        final var depth = getSkirtDepth(factor);
        for (int row = 0; row < layout.getChunksZ(); row++)
            for (int column = 0; column < layout.getChunksX(); column++) {
                final var chunk = layout.getChunk(column, row);
                final var low = (layout.getMinimum(chunk) - 0.5f) * factor;
                final var high = (layout.getMaximum(chunk) - 0.5f) * factor;

                final var minX = (column / (float) layout.getChunksX() - 0.5f) * layout.getSize().x();
                final var minZ = (row / (float) layout.getChunksZ() - 0.5f) * layout.getSize().y();

                boxes[chunk].min().set(minX, Math.min(low, high) - depth, minZ);
                boxes[chunk].max().set(minX + layout.getChunkSizeX(), Math.max(low, high), minZ + layout.getChunkSizeZ());
            }
    }
}
