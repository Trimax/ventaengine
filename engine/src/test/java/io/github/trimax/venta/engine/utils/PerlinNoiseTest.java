package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

final class PerlinNoiseTest {
    @Test
    void testHeightmapSizeAndRange() {
        final var heightmap = PerlinNoise.generate(32, 16, 42, 8.0, 4, 0.5, 0.0);

        assertEquals(32, heightmap.length, "Width must be respected");
        for (final var column : heightmap) {
            assertEquals(16, column.length, "Height must be respected");
            for (final var value : column)
                assertTrue(value >= 0.f && value <= 255.f, "Value must be in [0, 255], got " + value);
        }
    }

    @Test
    void testSameSeedGivesSameHeightmap() {
        final var first = PerlinNoise.generate(16, 16, 7, 4.0, 3, 0.5, 0.0);
        final var second = PerlinNoise.generate(16, 16, 7, 4.0, 3, 0.5, 0.0);

        assertTrue(Arrays.deepEquals(first, second), "Generation must be deterministic for a seed");
    }

    @Test
    void testDifferentSeedsGiveDifferentHeightmaps() {
        final var first = PerlinNoise.generate(16, 16, 1, 4.0, 3, 0.5, 0.0);
        final var second = PerlinNoise.generate(16, 16, 2, 4.0, 3, 0.5, 0.0);

        assertFalse(Arrays.deepEquals(first, second), "Different seeds must give different heightmaps");
    }

    @Test
    void testHeightmapIsNotFlat() {
        final var heightmap = PerlinNoise.generate(32, 32, 3, 8.0, 4, 0.5, 0.0);

        var min = Float.MAX_VALUE;
        var max = -Float.MAX_VALUE;
        for (final var column : heightmap)
            for (final var value : column) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }

        assertTrue(max - min > 10.f, "Noise must vary over the heightmap");
    }
}
