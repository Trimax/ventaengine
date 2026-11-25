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
        return harness.run(INCLUDES, "uniform vec2 position; uniform float scale;", "outputColor = vec4(getFoamLace(position, scale));",
                Map.of("position", new float[]{x, z}, "scale", scale))[0];
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

    @Test
    void testLaceChangesSmoothlyAcrossTheNeighbouringPoints() {
        var largest = 0.f;
        for (var x = 0.f; x < 10.f; x += 0.01f)
            largest = Math.max(largest, Math.abs(lace(x + 0.01f, 3.f, 2.f) - lace(x, 3.f, 2.f)));

        assertTrue(largest < 0.35f, "No jumps of the lace between points that are a hundredth apart: " + largest);
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
