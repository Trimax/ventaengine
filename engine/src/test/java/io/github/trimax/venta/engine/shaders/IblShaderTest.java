package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The functions for the convolution of a skybox (common/ibl.glsl) on the GPU: the sampling of the directions and the
 * integral of the BRDF.
 */
final class IblShaderTest {
    private static final float EPSILON = 1e-4f;
    private static final List<String> INCLUDES = List.of("common/ibl.glsl");

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @Test
    void testRadicalInverseMirrorsTheBitsAroundThePoint() {
        assertEquals(0.f, scalar("radicalInverse(0u)"), EPSILON);
        assertEquals(0.5f, scalar("radicalInverse(1u)"), EPSILON);
        assertEquals(0.25f, scalar("radicalInverse(2u)"), EPSILON);
        assertEquals(0.75f, scalar("radicalInverse(3u)"), EPSILON);
        assertEquals(0.125f, scalar("radicalInverse(4u)"), EPSILON);
        assertEquals(0.625f, scalar("radicalInverse(5u)"), EPSILON);
    }

    @Test
    void testHammersleyPointsFillTheSquareEvenly() {
        // The first coordinate is i / count, the points of the first half of the sequence are in both halves of the square
        for (int i = 0; i < 8; i++) {
            final var point = vec2("hammersley(" + i + "u, 8u)");

            assertEquals(i / 8.f, point[0], EPSILON, "x of the point " + i);
            assertTrue(point[1] >= 0.f && point[1] < 1.f, "y of the point " + i + " is in the square: " + point[1]);
        }

        // No two points have the same cell of the 8x8 grid: it is a low discrepancy sequence
        final var cells = new HashSet<String>();
        for (int i = 0; i < 8; i++) {
            final var point = vec2("hammersley(" + i + "u, 8u)");
            cells.add((int) (point[0] * 8) + ":" + (int) (point[1] * 8));
        }
        assertEquals(8, cells.size(), "The points are spread over the grid");
    }

    @Test
    void testCosineSamplesAreUnitVectorsInTheHemisphereAroundTheNormal() {
        final var normal = "normalize(vec3(0.3, -0.5, 0.8))";
        var sum = 0.f;
        final var count = 256;
        for (int i = 0; i < count; i++) {
            final var result = rgb("vec3(length(sampleHemisphereCosine(hammersley(" + i + "u, " + count + "u), " + normal + ")), "
                    + "dot(sampleHemisphereCosine(hammersley(" + i + "u, " + count + "u), " + normal + "), " + normal + "), 0.0)");

            assertEquals(1.f, result[0], EPSILON, "Sample " + i + " is a unit vector");
            assertTrue(result[1] >= -EPSILON, "Sample " + i + " is above the surface: " + result[1]);
            sum += result[1];
        }

        // The cosine of the angle of a cosine weighted sample is 2/3 on average
        assertEquals(2.f / 3.f, sum / count, 0.01f, "The average cosine");
    }

    @Test
    void testGgxHalfVectorsFollowTheNormalMoreCloselyOnSmoothSurfaces() {
        final var normal = "normalize(vec3(-0.2, 0.4, 0.9))";
        final var count = 128;

        var smooth = 0.f;
        var rough = 0.f;
        for (int i = 0; i < count; i++) {
            final var point = "hammersley(" + i + "u, " + count + "u)";

            smooth += rgb("vec3(dot(sampleHalfVectorGGX(" + point + ", " + normal + ", 0.1), " + normal + "))")[0];
            rough += rgb("vec3(dot(sampleHalfVectorGGX(" + point + ", " + normal + ", 0.9), " + normal + "))")[0];
        }

        assertTrue(smooth / count > rough / count, "The half vectors of a smooth surface are closer to the normal: " + smooth / count + " > " + rough / count);
        assertTrue(smooth / count > 0.95f, "A smooth surface is almost a mirror: " + smooth / count);
    }

    @Test
    void testHalfVectorOfThePointAtZeroIsTheNormal() {
        final var result = rgb("sampleHalfVectorGGX(vec2(0.3, 0.0), normalize(vec3(0.5, 0.5, 0.5)), 0.7) - normalize(vec3(0.5, 0.5, 0.5))");

        assertArrayEquals(new float[]{0.f, 0.f, 0.f}, result, EPSILON);
    }

    @Test
    void testCubeDirectionsOfTheCentersOfTheFacesAreTheAxes() {
        final var axes = new float[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

        for (int face = 0; face < axes.length; face++)
            assertArrayEquals(axes[face], rgb("getCubeDirection(" + face + ", vec2(0.0))"), EPSILON, "The center of the face " + face);
    }

    @Test
    void testCubeDirectionsAreUnitVectorsAndStayOnTheirFace() {
        for (int face = 0; face < 6; face++)
            for (final var a : new float[]{-1.f, -0.4f, 0.f, 0.7f, 1.f})
                for (final var b : new float[]{-1.f, 0.2f, 1.f}) {
                    final var direction = rgb("getCubeDirection(" + face + ", vec2(" + a + ", " + b + "))");
                    final var length = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2]);
                    final var axis = face / 2;
                    final var sign = face % 2 == 0 ? 1.f : -1.f;

                    assertEquals(1.0, length, 1e-4, "A unit vector on the face " + face);
                    assertTrue(direction[axis] * sign >= Math.abs(direction[(axis + 1) % 3]) - EPSILON
                                    && direction[axis] * sign >= Math.abs(direction[(axis + 2) % 3]) - EPSILON,
                            "The direction is on the face " + face + ": " + direction[0] + ", " + direction[1] + ", " + direction[2]);
                }
    }

    @Test
    void testBrdfOfAMirrorSeenStraightOnReflectsAllTheLight() {
        final var result = vec2("integrateBrdf(1.0, 0.1, 512)");

        assertTrue(result[0] > 0.9f, "The scale of the fresnel is about 1: " + result[0]);
        assertTrue(result[1] < 0.05f, "The bias of the fresnel is about 0: " + result[1]);
    }

    @Test
    void testBrdfNeverReflectsMoreThanAllTheLight() {
        for (final var roughness : new float[]{0.05f, 0.3f, 0.7f, 1.f})
            for (final var cosine : new float[]{0.05f, 0.3f, 0.7f, 1.f}) {
                final var result = vec2("integrateBrdf(" + cosine + ", " + roughness + ", 512)");

                assertTrue(result[0] >= 0.f && result[1] >= 0.f, "No negative light at " + cosine + ", " + roughness);
                assertTrue(result[0] + result[1] <= 1.02f, "No more than all the light at " + cosine + ", " + roughness + ": " + (result[0] + result[1]));
            }
    }

    @Test
    void testRoughSurfaceLosesTheLightOfTheMirror() {
        final var smooth = vec2("integrateBrdf(0.5, 0.2, 512)");
        final var rough = vec2("integrateBrdf(0.5, 1.0, 512)");

        assertTrue(rough[0] + rough[1] < smooth[0] + smooth[1], "A rough surface reflects less (it masks itself): " + (rough[0] + rough[1]) + " < " + (smooth[0] + smooth[1]));
    }

    @Test
    void testFresnelBiasGrowsAtGrazingAngles() {
        final var straight = vec2("integrateBrdf(1.0, 0.3, 512)");
        final var grazing = vec2("integrateBrdf(0.1, 0.3, 512)");

        assertTrue(grazing[1] > straight[1], "More of the reflection is the fresnel at the grazing angle: " + grazing[1] + " > " + straight[1]);
    }

    private static float scalar(final String expression) {
        return harness.run(INCLUDES, "", "outputColor = vec4(" + expression + ");", Map.of())[0];
    }

    private static float[] rgb(final String expression) {
        final var color = harness.run(INCLUDES, "", "outputColor = vec4(" + expression + ", 1.0);", Map.of());

        return new float[]{color[0], color[1], color[2]};
    }

    private static float[] vec2(final String expression) {
        final var color = harness.run(INCLUDES, "", "outputColor = vec4(" + expression + ", 0.0, 1.0);", Map.of());

        return new float[]{color[0], color[1]};
    }
}
