package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The fade of the waves with the distance (common/water-waves.glsl), the swash of the water (common/swash.glsl) and the wet ground
 * of a shore (common/terrain-wet.glsl) on the GPU.
 */
final class WaterSwashShaderTest {
    private static final float EPSILON = 1e-3f;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float fade(final float distance, final float fadeDistance) {
        return harness.run(List.of("common/water-waves.glsl"), "uniform float distance; uniform float fadeDistance;",
                "outputColor = vec4(getWaveFade(distance, fadeDistance));", Map.of("distance", distance, "fadeDistance", fadeDistance))[0];
    }

    @Test
    void testWavesAreFullNearAndGoneAtTheDistance() {
        assertEquals(1.f, fade(0.f, 1000.f), EPSILON);
        assertEquals(1.f, fade(300.f, 1000.f), EPSILON, "The first third of the distance keeps them");
        assertEquals(0.f, fade(1000.f, 1000.f), EPSILON);
        assertEquals(0.f, fade(5000.f, 1000.f), EPSILON);
    }

    @Test
    void testWavesFadeSmoothlyAndNeverGrowWithTheDistance() {
        var previous = 1.f;
        for (var distance = 0.f; distance <= 1100.f; distance += 25.f) {
            final var value = fade(distance, 1000.f);
            assertTrue(value <= previous + EPSILON, "Farther is not stronger: " + value + " after " + previous);
            previous = value;
        }

        final var middle = fade(650.f, 1000.f);
        assertTrue(middle > 0.1f && middle < 0.9f, "In between: " + middle);
    }

    @Test
    void testNoFadeDistanceKeepsTheWaves() {
        assertEquals(1.f, fade(0.f, 0.f), EPSILON);
        assertEquals(1.f, fade(99999.f, 0.f), EPSILON);
    }

    private static float swash(final float amplitude, final float period, final float time) {
        return harness.run(List.of("common/swash.glsl"), "uniform float amplitude; uniform float period; uniform float time;",
                "outputColor = vec4(getSwash(amplitude, period, time));", Map.of("amplitude", amplitude, "period", period, "time", time))[0];
    }

    @Test
    void testSwashGoesUpAndDownOnceInAPeriod() {
        assertEquals(0.5f * 0.4f, swash(0.4f, 8.f, 0.f), EPSILON, "It starts in the middle");
        assertEquals(0.4f, swash(0.4f, 8.f, 2.f), EPSILON, "The highest at a quarter of the period");
        assertEquals(0.f, swash(0.4f, 8.f, 6.f), EPSILON, "The lowest after three quarters");
        assertEquals(swash(0.4f, 8.f, 1.3f), swash(0.4f, 8.f, 9.3f), EPSILON, "And the same a period later");
    }

    @Test
    void testSwashStaysBetweenNothingAndItsAmplitude() {
        for (var time = 0.f; time < 20.f; time += 0.37f) {
            final var value = swash(0.7f, 5.f, time);
            assertTrue(value >= -EPSILON && value <= 0.7f + EPSILON, "At " + time + ": " + value);
        }
    }

    @Test
    void testNoAmplitudeOrNoPeriodIsNoSwash() {
        assertEquals(0.f, swash(0.f, 8.f, 3.f), EPSILON);
        assertEquals(0.f, swash(0.4f, 0.f, 3.f), EPSILON);
    }

    private static float wet(final float height, final float wetHeight, final float amplitude, final float time, final float irregularity) {
        return harness.run(List.of("common/terrain-wet.glsl"),
                "uniform float height; uniform float wetHeight; uniform float amplitude; uniform float time; uniform float irregularity;",
                "outputColor = vec4(getWetness(height, 0.0, wetHeight, amplitude, 8.0, time, irregularity));",
                Map.of("height", height, "wetHeight", wetHeight, "amplitude", amplitude, "time", time, "irregularity", irregularity))[0];
    }

    @Test
    void testGroundUnderTheWaterIsWetAndHighGroundIsDry() {
        assertEquals(1.f, wet(-3.f, 0.6f, 0.f, 0.f, 0.f), EPSILON, "Under the water");
        assertEquals(1.f, wet(0.f, 0.6f, 0.f, 0.f, 0.f), EPSILON, "At the water");
        assertEquals(0.f, wet(5.f, 0.6f, 0.f, 0.f, 0.f), EPSILON, "High above it");
    }

    @Test
    void testWetnessFadesAboveTheWater() {
        var previous = 1.f;
        for (var height = 0.f; height < 1.f; height += 0.05f) {
            final var value = wet(height, 0.6f, 0.f, 0.f, 0.f);
            assertTrue(value <= previous + EPSILON, "Higher is not wetter: " + value + " after " + previous);
            previous = value;
        }

        final var middle = wet(0.3f, 0.6f, 0.f, 0.f, 0.f);
        assertTrue(middle > 0.1f && middle < 0.9f, "Half way up the fade: " + middle);
    }

    @Test
    void testWetLineFollowsTheSwash() {
        // A point a bit above the water: it is wet when the swash is up and drier when it is down
        final var up = wet(0.5f, 0.6f, 0.6f, 2.f, 0.f);
        final var down = wet(0.5f, 0.6f, 0.6f, 6.f, 0.f);

        assertTrue(up > down + 0.2f, "The water is at it: " + up + " against " + down);
    }

    @Test
    void testSandStaysDampWhereTheWaterReachedAndLeft() {
        final var damp = wet(0.7f, 0.6f, 0.6f, 6.f, 0.f);

        assertTrue(damp > 0.2f, "Under the highest point of the swash it is damp: " + damp);
        assertTrue(damp < 0.7f, "But not as wet as under the water");
        assertEquals(0.f, wet(3.f, 0.6f, 0.6f, 6.f, 0.f), EPSILON, "Far above the reach it is dry");
    }

    @Test
    void testIrregularityMovesTheLine() {
        final var plain = wet(0.4f, 0.6f, 0.f, 0.f, 0.f);
        final var raised = wet(0.4f, 0.6f, 0.f, 0.f, 0.3f);
        final var lowered = wet(0.4f, 0.6f, 0.f, 0.f, -0.3f);

        assertTrue(raised > plain && plain > lowered, "The line goes up and down: " + lowered + ", " + plain + ", " + raised);
    }
}
