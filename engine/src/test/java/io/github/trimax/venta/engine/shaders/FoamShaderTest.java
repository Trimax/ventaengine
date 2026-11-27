package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The lace of the foam (common/foam.glsl) on the GPU.
 */
final class FoamShaderTest {
    private static final float EPSILON = 1e-4f;
    private static final List<String> INCLUDES = List.of("common/foam.glsl");

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float lace(final float x, final float z, final float scale) {
        return lace(x, z, scale, 0.f);
    }

    private static float lace(final float x, final float z, final float scale, final float footprint) {
        return harness.run(INCLUDES, "uniform vec2 position; uniform float scale; uniform float footprint;", "outputColor = vec4(getFoamLace(position, scale, footprint));",
                Map.of("position", new float[]{x, z}, "scale", scale, "footprint", footprint))[0];
    }

    private static float[] distances(final float x, final float z) {
        return harness.run(INCLUDES, "uniform vec2 position;", "outputColor = vec4(getFoamDistances(position), 0.0, 1.0);",
                Map.of("position", new float[]{x, z}));
    }

    @Test
    void testLaceIsBetweenNothingAndAll() {
        for (var x = 0.f; x < 20.f; x += 0.37f)
            for (var z = 0.f; z < 20.f; z += 0.53f) {
                final var value = lace(x, z, 2.f);
                assertTrue(value >= 0.f && value <= 1.f, "At " + x + ", " + z + ": " + value);
            }
    }

    @Test
    void testLaceIsTheSameAtTheSamePlace() {
        assertEquals(lace(3.2f, 7.9f, 2.f), lace(3.2f, 7.9f, 2.f), EPSILON);
    }

    @Test
    void testLaceHasBubblesAndGaps() {
        var bubbles = 0;
        var gaps = 0;
        for (var x = 0.f; x < 30.f; x += 0.21f)
            for (var z = 0.f; z < 30.f; z += 0.29f) {
                final var value = lace(x, z, 2.f);
                if (value > 0.7f)
                    bubbles++;

                if (value < 0.2f)
                    gaps++;
            }

        assertTrue(bubbles > 50, "There is foam: " + bubbles);
        assertTrue(gaps > 50, "And there are gaps in it: " + gaps);
    }

    private static float film(final float x, final float z, final float seed) {
        return harness.run(INCLUDES, "uniform vec2 position; uniform float seed;", "outputColor = vec4(getFoamFilm(position, seed));",
                Map.of("position", new float[]{x, z}, "seed", seed))[0];
    }

    @Test
    void testFilmHasHolesAndWallsBetweenThem() {
        var holes = 0;
        var walls = 0;
        var total = 0;
        for (var x = 0.f; x < 20.f; x += 0.19f)
            for (var z = 0.f; z < 20.f; z += 0.23f) {
                final var value = film(x, z, 0.f);
                assertTrue(value >= 0.f && value <= 1.f, "At " + x + ", " + z + ": " + value);

                total++;
                if (value < 0.5f)
                    holes++;

                if (value > 0.9f)
                    walls++;
            }

        assertTrue(holes > total * 0.05f && holes < total * 0.6f, "There are holes, and not only holes: " + holes + " of " + total);
        assertTrue(walls > total * 0.2f, "And there is film between them: " + walls + " of " + total);
    }

    @Test
    void testFilmIsDifferentForDifferentSeeds() {
        var different = 0;
        for (var x = 0.f; x < 10.f; x += 0.31f)
            if (Math.abs(film(x, 2.f, 0.f) - film(x, 2.f, 5.f)) > 0.2f)
                different++;

        assertTrue(different > 3, "The holes of two films are not the same: " + different);
    }

    @Test
    void testLaceDoesNotRepeatLikeAGrid() {
        // A net of equal cells is the same after a cell, the lace is not: its holes have different sizes and the plane is bent
        var different = 0;
        var total = 0;
        for (var x = 0.f; x < 12.f; x += 0.47f)
            for (var z = 0.f; z < 12.f; z += 0.53f) {
                total++;
                if (Math.abs(lace(x, z, 2.f) - lace(x + 0.5f, z, 2.f)) > 0.2f)
                    different++;
            }

        assertTrue(different > total * 0.15f, "The lace after a cell is another lace: " + different + " of " + total);
    }

    @Test
    void testFoamFarAwayIsItsAverageAndHasNoHoles() {
        // A pixel that covers meters of the water can't show the holes: the foam is a soft film, the same from one pixel to the next
        var smallest = 1.f;
        var largest = 0.f;
        for (var x = 0.f; x < 8.f; x += 0.37f)
            for (var z = 0.f; z < 8.f; z += 0.41f) {
                final var value = lace(x, z, 2.f, 5.f);
                smallest = Math.min(smallest, value);
                largest = Math.max(largest, value);
            }

        assertTrue(smallest > 0.12f, "No black holes: " + smallest);
        assertTrue(largest < 1.f, "And no white walls: " + largest);

        // Near, with the footprint of nothing, the holes are there
        var holes = 0;
        for (var x = 0.f; x < 8.f; x += 0.37f)
            for (var z = 0.f; z < 8.f; z += 0.41f)
                if (lace(x, z, 2.f, 0.f) < 0.1f)
                    holes++;

        assertTrue(holes > 5, "Near there are holes: " + holes);
    }

    @Test
    void testFoamIsNotANumberNeverAndNeverOutOfRange() {
        for (final var footprint : new float[]{0.f, -1.f, 0.001f, 0.2f, 3.f, 1000.f})
            for (var x = 0.f; x < 6.f; x += 0.7f) {
                final var value = lace(x, 1.f, 1.3f, footprint);
                assertTrue(value >= 0.f && value <= 1.f, "Footprint " + footprint + " at " + x + ": " + value);
            }
    }

    @Test
    void testLaceChangesSmoothlyAcrossTheNeighbouringPoints() {
        // The thin filaments are a few centimeters wide, so the points are a fifth of a centimeter apart: a jump is a break of the lace
        var largest = 0.f;
        var at = 0.f;
        for (var x = 0.f; x < 2.5f; x += 0.002f) {
            final var jump = Math.abs(lace(x + 0.002f, 3.f, 2.f) - lace(x, 3.f, 2.f));
            if (jump > largest) {
                largest = jump;
                at = x;
            }
        }

        assertTrue(largest < 0.35f, "No jumps of the lace between points that are two millimeters apart: " + largest + " at " + at
                + " (" + lace(at, 3.f, 2.f) + " and " + lace(at + 0.002f, 3.f, 2.f) + ")");
    }

    @Test
    void testNearestPointIsNearerThanTheSecondOne() {
        for (var x = 0.f; x < 6.f; x += 0.31f)
            for (var z = 0.f; z < 6.f; z += 0.43f) {
                final var result = distances(x, z);
                assertTrue(result[0] <= result[1], "At " + x + ", " + z + ": " + result[0] + " and " + result[1]);
                assertTrue(result[0] >= 0.f && result[1] < 3.f);
            }
    }

    @Test
    void testBiggerScaleMakesSmallerBubbles() {
        // The number of the changes of the lace along a line grows with the scale
        assertTrue(crossings(1.f) < crossings(4.f), "Finer lace with a bigger scale: " + crossings(1.f) + " against " + crossings(4.f));
    }

    private static int crossings(final float scale) {
        var count = 0;
        var above = lace(0.f, 1.f, scale) > 0.5f;
        for (var x = 0.f; x < 20.f; x += 0.05f) {
            final var now = lace(x, 1.f, scale) > 0.5f;
            if (now != above)
                count++;

            above = now;
        }

        return count;
    }
}
