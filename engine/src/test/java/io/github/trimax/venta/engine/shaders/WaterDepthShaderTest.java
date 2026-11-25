package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * How deep the water looks (common/water-depth.glsl) on the GPU: the light that comes through it, the foam of the shore and
 * the shift of the bottom seen through the ripples.
 */
final class WaterDepthShaderTest {
    private static final float EPSILON = 1e-3f;
    private static final List<String> INCLUDES = List.of("common/water-depth.glsl");

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float transmittance(final float thickness, final float distance) {
        return harness.run(INCLUDES, "uniform float thickness; uniform float distance;",
                "outputColor = vec4(getWaterTransmittance(thickness, distance));", Map.of("thickness", thickness, "distance", distance))[0];
    }

    @Test
    void testNoWaterLetsAllTheLightThrough() {
        assertEquals(1.f, transmittance(0.f, 8.f), EPSILON);
        assertEquals(1.f, transmittance(-2.f, 8.f), EPSILON, "A negative thickness is no water");
    }

    @Test
    void testDepthDistanceHidesAlmostEverything() {
        assertEquals(0.05f, transmittance(8.f, 8.f), 0.002f, "5% at the depth distance");
        assertTrue(transmittance(40.f, 8.f) < 1e-4f, "The deep is dark");
    }

    @Test
    void testDeeperWaterLetsLessThrough() {
        var previous = 1.f;
        for (var thickness = 0.5f; thickness < 20.f; thickness += 0.5f) {
            final var value = transmittance(thickness, 8.f);
            assertTrue(value < previous, "It falls with the thickness: " + value + " after " + previous);
            previous = value;
        }
    }

    @Test
    void testLongerDepthDistanceMakesWaterClearer() {
        assertTrue(transmittance(5.f, 30.f) > transmittance(5.f, 5.f));
    }

    @Test
    void testTransmittanceIsTheExponentialOfTheThickness() {
        // Two layers of water let through what their two transmittances give together
        assertEquals(transmittance(2.f, 8.f) * transmittance(3.f, 8.f), transmittance(5.f, 8.f), EPSILON);
    }

    private static float foam(final float thickness, final float width, final float intensity, final float pattern) {
        return harness.run(INCLUDES, "uniform float thickness; uniform float width; uniform float intensity; uniform float pattern;",
                "outputColor = vec4(getShoreFoam(thickness, width, intensity, pattern));",
                Map.of("thickness", thickness, "width", width, "intensity", intensity, "pattern", pattern))[0];
    }

    @Test
    void testFoamIsWhiteAtTheEdgeAndGoneAtTheWidth() {
        assertEquals(0.8f, foam(0.f, 1.2f, 0.8f, 1.f), EPSILON, "At the edge, with the full pattern, it is the intensity");
        assertEquals(0.f, foam(1.2f, 1.2f, 0.8f, 1.f), EPSILON);
        assertEquals(0.f, foam(5.f, 1.2f, 0.8f, 1.f), EPSILON, "Deep water has none");
    }

    @Test
    void testFoamFadesWithTheThickness() {
        var previous = 1.f;
        for (var thickness = 0.f; thickness <= 1.2f; thickness += 0.1f) {
            final var value = foam(thickness, 1.2f, 1.f, 1.f);
            assertTrue(value <= previous + EPSILON, "It never grows away from the shore: " + value + " after " + previous);
            previous = value;
        }
    }

    @Test
    void testPatternBreaksTheFoamIntoPatches() {
        final var full = foam(0.2f, 1.2f, 1.f, 1.f);
        final var broken = foam(0.2f, 1.2f, 1.f, 0.f);

        assertTrue(full > broken && broken > 0.f, "A dim spot of the pattern has less foam, not none: " + broken + " against " + full);
    }

    @Test
    void testNoWidthOrNoIntensityMeansNoFoam() {
        assertEquals(0.f, foam(0.f, 0.f, 1.f, 1.f), EPSILON, "No width");
        assertEquals(0.f, foam(0.f, 1.2f, 0.f, 1.f), EPSILON, "No intensity");
    }

    private static float[] refract(final float[] normal, final float strength, final float thickness, final float distance) {
        return harness.run(INCLUDES, "uniform vec3 normal; uniform float strength; uniform float thickness; uniform float distance;",
                "outputColor = vec4(getRefractedCoordinates(vec2(0.5, 0.25), normal, strength, thickness, distance), 0.0, 1.0);",
                Map.of("normal", normal, "strength", strength, "thickness", thickness, "distance", distance));
    }

    @Test
    void testFlatWaterDoesNotShiftTheBottom() {
        final var result = refract(new float[]{0, 1, 0}, 0.1f, 10.f, 8.f);

        assertEquals(0.5f, result[0], EPSILON);
        assertEquals(0.25f, result[1], EPSILON);
    }

    @Test
    void testTiltedWaterShiftsTheBottomAlongTheTilt() {
        final var result = refract(new float[]{0.3f, 0.9f, -0.2f}, 0.1f, 10.f, 8.f);

        assertEquals(0.5f + 0.03f, result[0], EPSILON);
        assertEquals(0.25f - 0.02f, result[1], EPSILON);
    }

    @Test
    void testShiftFadesOutInTheShallows() {
        final var deep = refract(new float[]{0.3f, 0.9f, 0.f}, 0.1f, 10.f, 8.f)[0] - 0.5f;
        final var shallow = refract(new float[]{0.3f, 0.9f, 0.f}, 0.1f, 0.2f, 8.f)[0] - 0.5f;
        final var none = refract(new float[]{0.3f, 0.9f, 0.f}, 0.1f, 0.f, 8.f)[0] - 0.5f;

        assertTrue(shallow < deep && shallow > 0.f, "A little shift in the shallows: " + shallow + " against " + deep);
        assertEquals(0.f, none, EPSILON, "None where there is no water at all");
    }

    @Test
    void testZeroStrengthIsNoRefraction() {
        final var result = refract(new float[]{0.5f, 0.5f, 0.5f}, 0.f, 10.f, 8.f);

        assertEquals(0.5f, result[0], EPSILON);
        assertEquals(0.25f, result[1], EPSILON);
    }

    private static float bands(final float thickness, final float width, final float intensity, final float time) {
        return harness.run(INCLUDES, "uniform float thickness; uniform float width; uniform float intensity; uniform float time;",
                "outputColor = vec4(getFoamBands(thickness, width, intensity, time));",
                Map.of("thickness", thickness, "width", width, "intensity", intensity, "time", time))[0];
    }

    @Test
    void testBandsAreOnlyWhereTheWaterIsThinEnoughToBreak() {
        for (var time = 0.f; time < 10.f; time += 0.5f) {
            assertEquals(0.f, bands(0.f, 1.2f, 1.f, time), EPSILON, "At the very edge there are the foam of the shore, not the bands");
            assertEquals(0.f, bands(15.f, 1.2f, 1.f, time), EPSILON, "Deep water has no bands");
        }
    }

    @Test
    void testBandsMoveToTheShoreWithTime() {
        // A line of foam is at some thickness at a time and at a smaller one a while after: the waves go in
        final var width = 1.2f;
        float lineAtStart = -1.f;
        float lineLater = -1.f;
        for (var thickness = 2.f; thickness < 9.f; thickness += 0.02f) {
            if (lineAtStart < 0.f && bands(thickness, width, 1.f, 0.f) > 0.9f)
                lineAtStart = thickness;

            if (lineLater < 0.f && bands(thickness, width, 1.f, 1.5f) > 0.9f)
                lineLater = thickness;
        }

        assertTrue(lineAtStart > 0.f && lineLater > 0.f, "Lines are found: " + lineAtStart + ", " + lineLater);
        assertTrue(lineLater != lineAtStart, "The lines have moved");
    }

    @Test
    void testBandsAreSeveralInARowAndHaveGapsBetween() {
        var lines = 0;
        var previous = false;
        var gaps = 0;
        for (var thickness = 2.f; thickness < 11.f; thickness += 0.02f) {
            final var value = bands(thickness, 1.2f, 1.f, 0.f);
            final var on = value > 0.5f;
            if (on && !previous)
                lines++;

            if (!on && value < 0.05f)
                gaps++;

            previous = on;
        }

        assertTrue(lines >= 2, "More than one line: " + lines);
        assertTrue(gaps > 0, "And clear water between them");
    }

    @Test
    void testBandsNeedAWidthAndAnIntensity() {
        assertEquals(0.f, bands(3.f, 0.f, 1.f, 0.f), EPSILON);
        assertEquals(0.f, bands(3.f, 1.2f, 0.f, 0.f), EPSILON);
    }

    @Test
    void testBandsAreNotBrighterThanTheirIntensity() {
        for (var thickness = 1.f; thickness < 12.f; thickness += 0.1f)
            assertTrue(bands(thickness, 1.2f, 0.4f, 2.f) <= 0.4f + EPSILON, "At " + thickness);
    }

    private static float shoreFade(final float depth, final float edge) {
        return harness.run(INCLUDES, "uniform float depth; uniform float edge;", "outputColor = vec4(getShoreFade(depth, edge));",
                Map.of("depth", depth, "edge", edge))[0];
    }

    @Test
    void testWaterIsClearAtTheEdgeAndOpaqueFromTheEdgeDepth() {
        assertEquals(0.f, shoreFade(0.f, 0.15f), EPSILON, "No water over the ground");
        assertEquals(0.f, shoreFade(-1.f, 0.15f), EPSILON, "The ground over the water");
        assertEquals(1.f, shoreFade(0.15f, 0.15f), EPSILON);
        assertEquals(1.f, shoreFade(30.f, 0.15f), EPSILON);
    }

    @Test
    void testWaterFadesInSmoothlyWithTheDepth() {
        var previous = 0.f;
        for (var depth = 0.f; depth <= 0.2f; depth += 0.01f) {
            final var value = shoreFade(depth, 0.15f);
            assertTrue(value >= previous - EPSILON, "Deeper is not clearer: " + value + " after " + previous);
            previous = value;
        }

        final var middle = shoreFade(0.075f, 0.15f);
        assertTrue(middle > 0.1f && middle < 0.9f, "Half way it is neither: " + middle);
    }

    @Test
    void testNoEdgeKeepsTheWaterOpaque() {
        assertEquals(1.f, shoreFade(0.f, 0.f), EPSILON);
    }
}
