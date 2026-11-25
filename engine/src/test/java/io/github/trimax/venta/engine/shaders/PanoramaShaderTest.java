package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Where a direction is in a panorama of the sky (common/panorama.glsl) on the GPU.
 */
final class PanoramaShaderTest {
    private static final float EPSILON = 1e-4f;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float[] coordinates(final float x, final float y, final float z) {
        return harness.run(List.of("common/panorama.glsl"), "uniform vec3 direction;",
                "outputColor = vec4(getPanoramaCoordinates(direction), 0.0, 1.0);", Map.of("direction", new float[]{x, y, z}));
    }

    @Test
    void testTheCenterOfThePictureLooksAlongMinusZ() {
        final var result = coordinates(0.f, 0.f, -1.f);

        assertEquals(0.5f, result[0], EPSILON);
        assertEquals(0.5f, result[1], EPSILON, "On the horizon");
    }

    @Test
    void testTheRightOfThePictureGoesToPlusX() {
        assertEquals(0.75f, coordinates(1.f, 0.f, 0.f)[0], EPSILON, "A quarter of the way round to the right");
        assertEquals(0.25f, coordinates(-1.f, 0.f, 0.f)[0], EPSILON, "And to the left");
    }

    @Test
    void testBehindIsAtTheEdgeOfThePicture() {
        final var u = coordinates(0.f, 0.f, 1.f)[0];

        assertTrue(u < EPSILON || u > 1.f - EPSILON, "The seam of the picture is behind: " + u);
    }

    @Test
    void testTheTopOfThePictureIsUp() {
        assertEquals(0.f, coordinates(0.f, 1.f, 0.f)[1], EPSILON);
        assertEquals(1.f, coordinates(0.f, -1.f, 0.f)[1], EPSILON);
        assertTrue(coordinates(0.f, 0.5f, -1.f)[1] < 0.5f, "Above the horizon is higher in the picture");
        assertTrue(coordinates(0.f, -0.5f, -1.f)[1] > 0.5f);
    }

    @Test
    void testLengthOfTheDirectionDoesNotMatter() {
        final var short_ = coordinates(0.3f, 0.2f, -0.5f);
        final var long_ = coordinates(3.f, 2.f, -5.f);

        assertEquals(short_[0], long_[0], EPSILON);
        assertEquals(short_[1], long_[1], EPSILON);
    }

    @Test
    void testTurningRoundTheVerticalAxisMovesAlongThePicture() {
        // Looking from -z towards +x the picture goes from its middle to its right: the longitude grows
        var previous = coordinates(0.f, 0.f, -1.f)[0];
        for (var angle = 0.1f; angle < 1.5f; angle += 0.1f) {
            final var u = coordinates((float) Math.sin(angle), 0.f, (float) -Math.cos(angle))[0];
            assertTrue(u > previous, "To the right: " + u + " after " + previous);
            previous = u;
        }
    }
}
