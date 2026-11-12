package io.github.trimax.venta.engine.model.common.terrain;

import org.joml.Vector2fc;
import org.joml.Vector2ic;

import lombok.Getter;
import lombok.NonNull;

/**
 * Heightmap values on the CPU side. They are sampled the same way the terrain vertex shader samples the heightmap
 * texture: linear filtering between texel centers and repeat wrapping.
 */
public final class Heightmap {
    @Getter
    private final int width;

    @Getter
    private final int height;

    private final float[] values;

    /**
     * Creates a heightmap from texel values.
     *
     * @param width  width in texels
     * @param height height in texels
     * @param values row by row texel values in [0, 1], the first row is v = 0
     */
    public Heightmap(final int width, final int height, final float @NonNull [] values) {
        if (width < 1 || height < 1)
            throw new IllegalArgumentException(String.format("Heightmap must have at least one texel, got %dx%d", width, height));

        if (values.length != width * height)
            throw new IllegalArgumentException(String.format("Heightmap %dx%d must have %d values, got %d", width, height, width * height, values.length));

        this.width = width;
        this.height = height;
        this.values = values;
    }

    /**
     * Value at the texture coordinates, like texture() with GL_LINEAR and GL_REPEAT
     */
    public float sample(final float u, final float v) {
        final var x = u * width - 0.5f;
        final var y = v * height - 0.5f;

        final var column = (int) Math.floor(x);
        final var row = (int) Math.floor(y);

        final var fractionX = x - column;
        final var fractionY = y - row;

        final var top = lerp(get(column, row), get(column + 1, row), fractionX);
        final var bottom = lerp(get(column, row + 1), get(column + 1, row + 1), fractionX);
        return lerp(top, bottom, fractionY);
    }

    /**
     * Value at the point of a grid mesh (local coordinates). The heightmap is sampled at the grid vertices and
     * interpolated over the triangle containing the point, so the value matches the rendered surface.
     * Points outside the grid get the value of the nearest edge.
     */
    public float sample(@NonNull final Vector2fc size, @NonNull final Vector2ic segments, final float x, final float z) {
        final var cellX = Math.clamp((x / size.x() + 0.5f) * segments.x(), 0.f, segments.x());
        final var cellZ = Math.clamp((z / size.y() + 0.5f) * segments.y(), 0.f, segments.y());

        final var column = Math.min((int) cellX, segments.x() - 1);
        final var row = Math.min((int) cellZ, segments.y() - 1);

        final var fractionX = cellX - column;
        final var fractionZ = cellZ - row;

        final var topLeft = sampleVertex(segments, column, row);
        final var topRight = sampleVertex(segments, column + 1, row);
        final var bottomLeft = sampleVertex(segments, column, row + 1);
        final var bottomRight = sampleVertex(segments, column + 1, row + 1);

        // A cell is split along its diagonal from the bottom left to the top right vertex (see GeometryUtil.createGrid)
        if (fractionX + fractionZ <= 1.f)
            return topLeft + fractionX * (topRight - topLeft) + fractionZ * (bottomLeft - topLeft);

        return bottomRight + (1.f - fractionX) * (bottomLeft - bottomRight) + (1.f - fractionZ) * (topRight - bottomRight);
    }

    private float sampleVertex(@NonNull final Vector2ic segments, final int column, final int row) {
        return sample((float) column / segments.x(), (float) row / segments.y());
    }

    private float get(final int column, final int row) {
        return values[Math.floorMod(row, height) * width + Math.floorMod(column, width)];
    }

    private static float lerp(final float from, final float to, final float fraction) {
        return from + (to - from) * fraction;
    }
}
