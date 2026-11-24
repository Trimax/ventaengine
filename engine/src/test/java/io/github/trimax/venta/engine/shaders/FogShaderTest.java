package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The exponential fog (common/fog.glsl) on the GPU: its thickness is the integral of a density that falls with the
 * height, and it is compared with the integral that is summed up in Java.
 */
final class FogShaderTest {
    private static final float EPSILON = 1e-4f;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    /** The uniforms of an exponential fog without the sun */
    private static Map<String, Object> fog(final float density, final float falloff, final float baseHeight) {
        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("fog.density", density);
        uniforms.put("fog.falloff", falloff);
        uniforms.put("fog.baseHeight", baseHeight);
        return uniforms;
    }

    private static float evaluate(final String expression, final Map<String, Object> uniforms, final float[] from, final float[] to) {
        final Map<String, Object> all = new HashMap<>(uniforms);
        all.put("from", from);
        all.put("to", to);

        return harness.run(List.of("common/fog.glsl"), "uniform vec3 from; uniform vec3 to;", "outputColor = vec4(" + expression + ");", all)[0];
    }

    /** 1 for an exponential fog, 0 for a fog of a layer */
    private static float exponential(final Map<String, Object> uniforms) {
        return harness.run(List.of("common/fog.glsl"), "", "outputColor = vec4(isFogExponential() ? 1.0 : 0.0);", uniforms)[0];
    }

    private static float thickness(final Map<String, Object> uniforms, final float[] from, final float[] to) {
        return evaluate("getFogThickness(from, to)", uniforms, from, to);
    }

    /** The integral of the density along the way, summed up in small steps */
    private static double summed(final float density, final float falloff, final float baseHeight, final float[] from, final float[] to) {
        final var steps = 20000;
        final var length = Math.sqrt(Math.pow(to[0] - from[0], 2) + Math.pow(to[1] - from[1], 2) + Math.pow(to[2] - from[2], 2));

        var sum = 0.0;
        for (int i = 0; i < steps; i++) {
            final var y = from[1] + (to[1] - from[1]) * (i + 0.5) / steps;
            sum += density * Math.exp(-falloff * (y - baseHeight)) * length / steps;
        }

        return sum;
    }

    @Test
    void testLevelWayIsTheDensityAtItsHeightTimesTheLength() {
        final var expected = 0.01f * (float) Math.exp(-0.02 * (30 - 10)) * 50.f;

        assertEquals(expected, thickness(fog(0.01f, 0.02f, 10.f), new float[]{0, 30, 0}, new float[]{30, 30, 40}), 1e-4f);
    }

    @Test
    void testThicknessIsTheIntegralOfTheDensityAlongTheWay() {
        final var uniforms = fog(0.02f, 0.03f, 5.f);

        for (final var way : new float[][][]{
                {{0, 0, 0}, {40, 25, 0}}, {{0, 60, 0}, {10, -20, 30}}, {{0, 3, 0}, {100, 4, 0}}, {{5, -30, 5}, {5, 80, 5}}}) {
            final var expected = summed(0.02f, 0.03f, 5.f, way[0], way[1]);

            assertEquals(expected, thickness(uniforms, way[0], way[1]), expected * 2e-3 + 1e-5, "From " + Arrays.toString(way[0]) + " to " + Arrays.toString(way[1]));
        }
    }

    @Test
    void testThicknessDoesNotDependOnTheDirectionOfTheWay() {
        final var uniforms = fog(0.02f, 0.03f, 0.f);
        final var from = new float[]{0, 2, 0};
        final var to = new float[]{30, 45, -20};

        assertEquals(thickness(uniforms, from, to), thickness(uniforms, to, from), 1e-4f);
    }

    @Test
    void testFogThinsOutWithTheHeight() {
        final var uniforms = fog(0.01f, 0.05f, 0.f);

        final var low = thickness(uniforms, new float[]{0, 0, 0}, new float[]{100, 0, 0});
        final var middle = thickness(uniforms, new float[]{0, 40, 0}, new float[]{100, 40, 0});
        final var high = thickness(uniforms, new float[]{0, 100, 0}, new float[]{100, 100, 0});

        assertTrue(low > middle && middle > high && high > 0.f, low + " " + middle + " " + high);
        assertEquals(low / (float) Math.exp(0.05 * 40), middle, 1e-3f, "e times less every 1 / falloff units");
    }

    @Test
    void testNoDistanceMeansNoFog() {
        final var uniforms = fog(0.01f, 0.05f, 0.f);

        assertEquals(0.f, thickness(uniforms, new float[]{3, 4, 5}, new float[]{3, 4, 5}), EPSILON);
        assertEquals(0.f, evaluate("getFogAmount(from, to)", uniforms, new float[]{3, 4, 5}, new float[]{3, 4, 5}), EPSILON);
    }

    @Test
    void testAmountIsTheLightThatIsLost() {
        final var uniforms = fog(0.01f, 0.02f, 0.f);
        final var from = new float[]{0, 0, 0};
        final var to = new float[]{0, 0, 100};

        final var amount = evaluate("getFogAmount(from, to)", uniforms, from, to);

        assertEquals(1.f - (float) Math.exp(-thickness(uniforms, from, to)), amount, EPSILON);
        assertTrue(amount > 0.f && amount < 1.f);
    }

    @Test
    void testAmountGrowsWithTheDistanceAndStaysBelowOne() {
        final var uniforms = fog(0.01f, 0.02f, 0.f);

        var previous = 0.f;
        for (var distance = 10.f; distance <= 5000.f; distance *= 2.f) {
            final var amount = evaluate("getFogAmount(from, to)", uniforms, new float[]{0, 0, 0}, new float[]{distance, 0, 0});

            assertTrue(amount > previous, "At " + distance);
            assertTrue(amount <= 1.f);
            previous = amount;
        }
    }

    @Test
    void testFarOffAndExtremeHeightsDoNotOverflow() {
        final var uniforms = fog(0.01f, 0.05f, 0.f);

        for (final var way : new float[][][]{
                {{0, 1e6f, 0}, {1000, 0, 0}}, {{0, -1e6f, 0}, {1000, 1e6f, 0}}, {{0, 0, 0}, {1e7f, 1e7f, 0}}, {{0, 1e9f, 0}, {0, 1e9f, 1e4f}}}) {
            final var amount = evaluate("getFogAmount(from, to)", uniforms, way[0], way[1]);

            assertTrue(Float.isFinite(amount) && amount >= 0.f && amount <= 1.f, "The amount is " + amount);
        }
    }

    @Test
    void testLayerFogIsUntouchedWithoutADensity() {
        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("fog.minimalDistance", 0.f);
        uniforms.put("fog.maximalDistance", 40.f);
        uniforms.put("fog.height", 1e30f);
        uniforms.put("fog.density", 0.f);

        assertEquals(0.5f, evaluate("getFogAmount(from, to)", uniforms, new float[]{0, 0, 0}, new float[]{20, 0, 0}), EPSILON);
        assertEquals(0.f, exponential(uniforms), "A fog without a density is a layer");
    }

    @Test
    void testFogIsExponentialWithADensity() {
        assertEquals(1.f, exponential(fog(0.001f, 0.02f, 0.f)));
    }

    /** The color of the fog along a way: red, green and blue of the color */
    private static float[] color(final Map<String, Object> uniforms, final float[] from, final float[] to) {
        final Map<String, Object> all = new HashMap<>(uniforms);
        all.put("from", from);
        all.put("to", to);

        return harness.run(List.of("common/fog.glsl"), "uniform vec3 from; uniform vec3 to;", "outputColor = vec4(getFogColor(from, to), 1.0);", all);
    }

    private static Map<String, Object> sunny(final float scattering, final float sharpness) {
        final var uniforms = fog(0.01f, 0.02f, 0.f);
        uniforms.put("fog.color", new float[]{0.5f, 0.5f, 0.6f});
        uniforms.put("fog.sunDirection", new float[]{1, 0, 0});
        uniforms.put("fog.sunColor", new float[]{4.f, 3.f, 2.f});
        uniforms.put("fog.sunScattering", scattering);
        uniforms.put("fog.sunSharpness", sharpness);
        return uniforms;
    }

    @Test
    void testFogHasItsOwnColorWithoutTheSun() {
        final var color = color(sunny(0.f, 8.f), new float[]{0, 0, 0}, new float[]{50, 0, 0});

        assertArrayEquals(new float[]{0.5f, 0.5f, 0.6f}, new float[]{color[0], color[1], color[2]}, EPSILON);
    }

    @Test
    void testFogTakesTheColorOfTheSunLookingAtIt() {
        final var color = color(sunny(1.f, 8.f), new float[]{0, 0, 0}, new float[]{50, 0, 0});

        assertArrayEquals(new float[]{4.f, 3.f, 2.f}, new float[]{color[0], color[1], color[2]}, EPSILON, "Straight at the sun");
    }

    @Test
    void testFogKeepsItsColorLookingAwayFromTheSun() {
        final var away = color(sunny(1.f, 8.f), new float[]{0, 0, 0}, new float[]{-50, 0, 0});
        final var aside = color(sunny(1.f, 8.f), new float[]{0, 0, 0}, new float[]{0, 0, 50});

        assertArrayEquals(new float[]{0.5f, 0.5f, 0.6f}, new float[]{away[0], away[1], away[2]}, EPSILON, "Away from the sun");
        assertArrayEquals(new float[]{0.5f, 0.5f, 0.6f}, new float[]{aside[0], aside[1], aside[2]}, EPSILON, "A right angle to the sun");
    }

    @Test
    void testGlowAroundTheSunIsNarrowerWithTheSharpness() {
        // 30 degrees from the sun: the cosine is 0.866
        final var way = new float[]{(float) Math.cos(Math.toRadians(30)) * 50.f, 0, (float) Math.sin(Math.toRadians(30)) * 50.f};

        final var wide = color(sunny(1.f, 2.f), new float[]{0, 0, 0}, way)[0];
        final var narrow = color(sunny(1.f, 40.f), new float[]{0, 0, 0}, way)[0];

        assertTrue(wide > narrow && narrow >= 0.5f, wide + " against " + narrow);
        assertEquals(0.5f + (4.f - 0.5f) * (float) Math.pow(Math.cos(Math.toRadians(30)), 2), wide, 2e-3f, "The lobe is cos ^ sharpness");
    }

    @Test
    void testScatteringScalesTheGlow() {
        final var full = color(sunny(1.f, 8.f), new float[]{0, 0, 0}, new float[]{50, 0, 0})[0];
        final var half = color(sunny(0.5f, 8.f), new float[]{0, 0, 0}, new float[]{50, 0, 0})[0];

        assertEquals(0.5f + (full - 0.5f) / 2.f, half, EPSILON);
    }

    @Test
    void testNoWayHasTheColorOfTheFogAndNoNan() {
        final var color = color(sunny(1.f, 8.f), new float[]{1, 2, 3}, new float[]{1, 2, 3});

        assertArrayEquals(new float[]{0.5f, 0.5f, 0.6f}, new float[]{color[0], color[1], color[2]}, EPSILON);
    }
}
