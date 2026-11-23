package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.utils.ColorUtil;

/**
 * Calls the functions of the common shader files on the GPU and compares the results with the values computed by
 * the formulas in Java.
 */
final class CommonShadersTest {
    private static final float EPSILON = 1e-4f;
    private static final float INFINITE_HEIGHT = 1e30f;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @Test
    void testFlagIsSetByANonZeroValue() {
        for (final var value : new int[]{-3, -1, 0, 1, 2, 100})
            assertEquals(value != 0 ? 1.f : 0.f, run(List.of("common/flags.glsl"), "uniform int value;",
                    "outputColor = vec4(isSet(value) ? 1.0 : 0.0);", Map.of("value", value))[0], "isSet(" + value + ")");
    }

    @Test
    void testFogFactorGrowsBetweenTheDistances() {
        final var fog = Map.<String, Object>of("fog.minimalDistance", 10.f, "fog.maximalDistance", 30.f);

        assertEquals(0.f, factor(0.f, fog), EPSILON, "Before the fog");
        assertEquals(0.f, factor(10.f, fog), EPSILON, "At the minimal distance");
        assertEquals(0.25f, factor(15.f, fog), EPSILON, "A quarter of the way");
        assertEquals(0.5f, factor(20.f, fog), EPSILON, "Half of the way");
        assertEquals(1.f, factor(30.f, fog), EPSILON, "At the maximal distance");
        assertEquals(1.f, factor(1000.f, fog), EPSILON, "Far away");
    }

    @Test
    void testFogFactorOfEmptyRangeDoesNotDivideByZero() {
        final var fog = Map.<String, Object>of("fog.minimalDistance", 10.f, "fog.maximalDistance", 10.f);

        assertEquals(0.f, factor(9.f, fog), EPSILON, "Before the distance");
        assertEquals(1.f, factor(11.f, fog), EPSILON, "After the distance");
    }

    @Test
    void testFogDistanceCountsThePartInTheLayer() {
        assertEquals(5.f, fogDistance(new float[]{0, 0, 0}, new float[]{0, 5, 0}, 10.f), EPSILON, "A way below the top of the layer");
        assertEquals(0.f, fogDistance(new float[]{0, 12, 0}, new float[]{3, 20, 4}, 10.f), EPSILON, "A way above the layer");
        assertEquals(10.f, fogDistance(new float[]{0, 0, 0}, new float[]{0, 20, 0}, 10.f), EPSILON, "Half of the way is in the layer");
        assertEquals(10.f, fogDistance(new float[]{0, 20, 0}, new float[]{0, 0, 0}, 10.f), EPSILON, "The direction doesn't matter");
        assertEquals(0.f, fogDistance(new float[]{0, 20, 0}, new float[]{50, 20, 0}, 10.f), EPSILON, "A horizontal way above the layer");
        assertEquals(50.f, fogDistance(new float[]{0, 5, 0}, new float[]{50, 5, 0}, 10.f), EPSILON, "A horizontal way in the layer");
        assertEquals(5.f, fogDistance(new float[]{0, 0, 0}, new float[]{3, 4, 0}, INFINITE_HEIGHT), EPSILON, "Without a layer the whole way is fogged");
    }

    @Test
    void testFogAmountIsTheFactorOfTheDistanceInTheLayer() {
        final var fog = Map.<String, Object>of("fog.minimalDistance", 0.f, "fog.maximalDistance", 40.f, "fog.height", 10.f,
                "from", new float[]{0, 0, 0}, "to", new float[]{0, 40, 0});

        assertEquals(10.f / 40.f, run(List.of("common/fog.glsl"), "uniform vec3 from; uniform vec3 to;",
                "outputColor = vec4(getFogAmount(from, to));", fog)[0], EPSILON, "A quarter of the fog distance is in the layer");
    }

    @Test
    void testAttenuation() {
        assertEquals(1.f, attenuation(1, 0, 0, 25.f), EPSILON, "A constant attenuation");
        assertEquals(1.f / 3.f, attenuation(1, 0.1f, 0.01f, 10.f), EPSILON, "1 / (1 + 0.1 * 10 + 0.01 * 100)");
        assertEquals(0.5f, attenuation(0, 0, 1, 1.41421356f), EPSILON, "Quadratic: 1 / 2");
        assertEquals(1.f / 0.0001f, attenuation(0, 0, 0, 5.f), 1.f, "An all-zero attenuation doesn't divide by zero");
    }

    @Test
    void testNoiseIsInRangeAndDeterministic() {
        var sum = 0.f;
        var count = 0;
        for (var x = -20.f; x < 20.f; x += 0.73f)
            for (var y = -20.f; y < 20.f; y += 0.61f) {
                final var first = noise(x, y);
                assertEquals(first, noise(x, y), 0.f, "The same point gives the same value");
                assertTrue(first >= 0.f && first <= 1.f, "The noise is in [0, 1]: " + first + " at " + x + ", " + y);
                sum += first;
                count++;
            }

        assertEquals(0.5f, sum / count, 0.08f, "The average of the noise is about the middle");
    }

    @Test
    void testNoiseIsContinuousAndEqualToHashInTheCornersOfCells() {
        for (final var corner : new float[][]{{0, 0}, {3, 7}, {-4, 2}, {10, -9}})
            assertEquals(hash(corner[0], corner[1]), noise(corner[0], corner[1]), EPSILON, "A corner of a cell has the value of its hash");

        for (var x = -5.f; x < 5.f; x += 0.37f)
            assertEquals(noise(x, 1.3f), noise(x + 0.001f, 1.3f), 0.01f, "Close points have close values at " + x);
    }

    @Test
    void testHashIsInRangeAndVaries() {
        final var values = new HashSet<Float>();
        for (var x = 0; x < 20; x++)
            for (var y = 0; y < 20; y++) {
                final var value = hash(x, y);
                assertTrue(value >= 0.f && value < 1.f, "The hash is in [0, 1): " + value);
                values.add(value);
            }

        // The sine of the GPU is approximate and the hash of its value is coarse, so some points of the lattice share a value
        assertTrue(values.size() > 100, "The hashes of 400 points are not a constant: " + values.size());
    }

    @Test
    void testSRGBMatchesTheConversionInJava() {
        for (var value = 0.f; value <= 1.f; value += 0.02f) {
            final var color = run(List.of("common/color.glsl"), "uniform float value;",
                    "outputColor = vec4(toSRGB(vec3(value, value * 0.5, value * 0.25)), 1.0);", Map.of("value", value));

            assertEquals(ColorUtil.toSRGB(value), color[0], EPSILON, "toSRGB of " + value);
            assertEquals(ColorUtil.toSRGB(value * 0.5f), color[1], EPSILON, "toSRGB of " + value * 0.5f);
            assertEquals(ColorUtil.toSRGB(value * 0.25f), color[2], EPSILON, "toSRGB of " + value * 0.25f);
        }
    }

    @Test
    void testNeutralToneMappingKeepsDarkTonesAndCompressesBrightOnes() {
        for (final var value : new float[]{0.f, 0.1f, 0.3f, 0.5f, 0.75f})
            assertEquals(value, toneMap("toneMapNeutral", value), EPSILON, "Colors below 0.76 stay as they are: " + value);

        var previous = 0.f;
        for (final var value : new float[]{0.8f, 1.f, 2.f, 5.f, 20.f, 1000.f}) {
            final var mapped = toneMap("toneMapNeutral", value);

            assertTrue(mapped > previous, "The brighter, the brighter: " + value);
            assertTrue(mapped <= 1.f + EPSILON, "Colors are compressed below white: " + value + " -> " + mapped);
            previous = mapped;
        }

        assertEquals(1.f, toneMap("toneMapNeutral", 1000.f), 0.01f, "Very bright colors are white");
    }

    @Test
    void testACESToneMappingMatchesTheFormula() {
        for (final var value : new float[]{0.f, 0.05f, 0.18f, 0.5f, 1.f, 3.f, 10.f}) {
            final var scaled = value * 0.6f;
            final var expected = Math.clamp((scaled * (2.51f * scaled + 0.03f)) / (scaled * (2.43f * scaled + 0.59f) + 0.14f), 0.f, 1.f);

            assertEquals(expected, toneMap("toneMapACES", value), EPSILON, "ACES of " + value);
        }

        assertEquals(0.f, toneMap("toneMapACES", 0.f), EPSILON, "Black stays black");
        assertEquals(1.f, toneMap("toneMapACES", 100.f), EPSILON, "Very bright colors are white");
    }

    private static float factor(final float distance, final Map<String, Object> fog) {
        final var uniforms = new HashMap<>(fog);
        uniforms.put("distance", distance);

        return run(List.of("common/fog.glsl"), "uniform float distance;", "outputColor = vec4(getFogFactor(distance));", uniforms)[0];
    }

    private static float fogDistance(final float[] from, final float[] to, final float height) {
        return run(List.of("common/fog.glsl"), "uniform vec3 from; uniform vec3 to;", "outputColor = vec4(getFogDistance(from, to));",
                Map.of("from", from, "to", to, "fog.height", height))[0];
    }

    private static float attenuation(final float constant, final float linear, final float quadratic, final float distance) {
        return run(List.of("common/lights.glsl"), "uniform vec4 arguments;",
                "outputColor = vec4(getAttenuation(Attenuation(arguments.x, arguments.y, arguments.z), arguments.w));",
                Map.of("arguments", new float[]{constant, linear, quadratic, distance}))[0];
    }

    private static float noise(final float x, final float y) {
        return run(List.of("common/noise.glsl"), "uniform vec2 point;", "outputColor = vec4(noise(point));", Map.of("point", new float[]{x, y}))[0];
    }

    private static float hash(final float x, final float y) {
        return run(List.of("common/noise.glsl"), "uniform vec2 point;", "outputColor = vec4(hash(point));", Map.of("point", new float[]{x, y}))[0];
    }

    private static float toneMap(final String function, final float value) {
        return run(List.of("common/color.glsl"), "uniform float value;", "outputColor = vec4(" + function + "(vec3(value)), 1.0);",
                Map.of("value", value))[0];
    }

    private static float[] run(final List<String> includes, final String declarations, final String code, final Map<String, Object> uniforms) {
        return harness.run(includes, declarations, code, uniforms);
    }
}
