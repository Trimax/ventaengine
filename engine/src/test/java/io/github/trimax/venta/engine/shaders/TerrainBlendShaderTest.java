package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;

/**
 * The blending of the materials of a terrain by the steepness of the ground and the triplanar sampling
 * (common/terrain-blend.glsl) on the GPU.
 */
final class TerrainBlendShaderTest {
    private static final float EPSILON = 1e-3f;
    private static final List<String> INCLUDES = List.of("common/terrain-blend.glsl");

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float[] run(final String declarations, final String code, final Map<String, Object> uniforms) {
        return harness.run(INCLUDES, declarations, code, uniforms);
    }

    private static float[] vec(final Vector3f vector) {
        return new float[]{vector.x, vector.y, vector.z};
    }

    @Test
    void testAngleOfTheGroundIsFromTheHorizon() {
        assertEquals(0.f, angle(new Vector3f(0, 1, 0)), 0.01f, "A plane");
        assertEquals(90.f, angle(new Vector3f(1, 0, 0)), 0.01f, "A wall");
        assertEquals(45.f, angle(new Vector3f(1, 1, 0).normalize()), 0.01f);
        assertEquals(90.f, angle(new Vector3f(0, -1, 0)), 0.01f, "A ceiling counts as a wall");
    }

    private float angle(final Vector3f normal) {
        return run("uniform vec3 normal;", "outputColor = vec4(getSlopeAngle(normal));", Map.of("normal", vec(normal)))[0];
    }

    private float weight(final float angle, final float minimum, final float maximum, final float blend) {
        return run("uniform float angle; uniform float minimum; uniform float maximum; uniform float blend;",
                "outputColor = vec4(getSlopeWeight(angle, minimum, maximum, blend));",
                Map.of("angle", angle, "minimum", minimum, "maximum", maximum, "blend", blend))[0];
    }

    @Test
    void testMaterialIsFullBetweenTheAnglesAndGoneFarOutsideOfThem() {
        assertEquals(1.f, weight(50.f, 35.f, 80.f, 8.f), EPSILON);
        assertEquals(1.f, weight(35.f, 35.f, 80.f, 8.f), EPSILON, "At the minimum it is full");
        assertEquals(1.f, weight(80.f, 35.f, 80.f, 8.f), EPSILON, "At the maximum it is full");
        assertEquals(0.f, weight(20.f, 35.f, 80.f, 8.f), EPSILON);
        assertEquals(0.f, weight(90.f, 35.f, 80.f, 8.f), EPSILON);
    }

    @Test
    void testMaterialFadesOverTheBlendOutsideOfTheAngles() {
        final var mid = weight(31.f, 35.f, 80.f, 8.f);
        assertTrue(mid > 0.f && mid < 1.f, "Inside the blend: " + mid);

        var previous = 0.f;
        for (var angle = 27.f; angle <= 35.f; angle += 1.f) {
            final var value = weight(angle, 35.f, 80.f, 8.f);
            assertTrue(value >= previous - EPSILON, "It grows up to the minimum: " + value + " after " + previous);
            previous = value;
        }

        assertEquals(0.f, weight(27.f, 35.f, 80.f, 8.f), EPSILON, "The blend ends 8 degrees before the minimum");
        assertEquals(0.f, weight(88.f, 35.f, 80.f, 8.f), EPSILON, "And 8 degrees after the maximum");
        assertTrue(weight(84.f, 35.f, 80.f, 8.f) > 0.f && weight(84.f, 35.f, 80.f, 8.f) < 1.f);
    }

    @Test
    void testMaterialThatStartsAtZeroOrEndsAtNinetyHasNoEdgeThere() {
        assertEquals(1.f, weight(0.f, 0.f, 30.f, 8.f), EPSILON, "A flat ground is the start of a gentle material");
        assertEquals(1.f, weight(90.f, 60.f, 90.f, 8.f), EPSILON, "A wall is the end of a steep one");
    }

    @Test
    void testHardEdgeWithoutABlend() {
        assertEquals(0.f, weight(34.9f, 35.f, 80.f, 0.f), EPSILON);
        assertEquals(1.f, weight(35.1f, 35.f, 80.f, 0.f), EPSILON);
        assertEquals(0.f, weight(80.1f, 35.f, 80.f, 0.f), EPSILON);
    }

    private float[] weights(final Vector3f normal) {
        return run("uniform vec3 normal;", "outputColor = vec4(getTriplanarWeights(normal), 1.0);", Map.of("normal", vec(normal)));
    }

    @Test
    void testPlaneOfTheMainAxisTakesTheSurface() {
        final var x = weights(new Vector3f(1, 0, 0));
        assertEquals(1.f, x[0], EPSILON);
        assertEquals(0.f, x[1], EPSILON);

        final var y = weights(new Vector3f(0, -1, 0));
        assertEquals(1.f, y[1], EPSILON, "The side doesn't matter");

        final var z = weights(new Vector3f(0, 0, 1));
        assertEquals(1.f, z[2], EPSILON);
    }

    @Test
    void testWeightsAddUpToOneAndFollowTheNormal() {
        for (final var normal : new Vector3f[]{new Vector3f(1, 1, 1).normalize(), new Vector3f(0.2f, 0.9f, 0.1f).normalize(), new Vector3f(-1, 0.3f, 0.5f).normalize()}) {
            final var result = weights(normal);
            assertEquals(1.f, result[0] + result[1] + result[2], EPSILON, "Normal " + normal);
            assertTrue(result[0] >= 0.f && result[1] >= 0.f && result[2] >= 0.f);

            final var largest = Math.abs(normal.x) > Math.abs(normal.y) && Math.abs(normal.x) > Math.abs(normal.z) ? 0 : Math.abs(normal.y) > Math.abs(normal.z) ? 1 : 2;
            for (int plane = 0; plane < 3; plane++)
                if (plane != largest)
                    assertTrue(result[largest] >= result[plane], "The plane of the main axis takes the most");
        }
    }

    @Test
    void testDiagonalNormalMixesThePlanesEqually() {
        final var result = weights(new Vector3f(1, 1, 1).normalize());

        assertEquals(1.f / 3.f, result[0], EPSILON);
        assertEquals(1.f / 3.f, result[1], EPSILON);
        assertEquals(1.f / 3.f, result[2], EPSILON);
    }

    private float[] coordinates(final int plane, final Vector3f position, final Vector3f normal) {
        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("plane", plane);
        uniforms.put("position", vec(position));
        uniforms.put("normal", vec(normal));

        return run("uniform int plane; uniform vec3 position; uniform vec3 normal;",
                "outputColor = vec4(getTriplanarCoordinates(plane, position, normal, vec2(10.0, 20.0)), 0.0, 1.0);", uniforms);
    }

    private float[] world(final int plane, final Vector3f normal, final Vector3f mapped) {
        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("plane", plane);
        uniforms.put("normal", vec(normal));
        uniforms.put("mapped", vec(mapped));

        return run("uniform int plane; uniform vec3 normal; uniform vec3 mapped;",
                "outputColor = vec4(getTriplanarNormal(plane, normal, mapped), 1.0);", uniforms);
    }

    private static final Vector3f[] FACES = {new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, -1, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};

    @Test
    void testFlatNormalMapGivesTheNormalOfTheSurface() {
        for (final var face : FACES) {
            final var plane = Math.abs(face.x) > 0.5f ? 0 : Math.abs(face.y) > 0.5f ? 1 : 2;
            final var result = world(plane, face, new Vector3f(0, 0, 1));

            assertEquals(face.x, result[0], EPSILON, "Face " + face);
            assertEquals(face.y, result[1], EPSILON);
            assertEquals(face.z, result[2], EPSILON);
        }
    }

    @Test
    void testTheDirectionsOfAPictureAreNotMirrored() {
        // red x green = blue on every side: the picture is seen from the front, so the bumps of the normal map are not turned inside out
        for (final var face : FACES) {
            final var plane = Math.abs(face.x) > 0.5f ? 0 : Math.abs(face.y) > 0.5f ? 1 : 2;
            final var red = new Vector3f(world(plane, face, new Vector3f(1, 0, 0)));
            final var green = new Vector3f(world(plane, face, new Vector3f(0, 1, 0)));
            final var blue = new Vector3f(world(plane, face, new Vector3f(0, 0, 1)));

            final var cross = red.cross(green, new Vector3f());
            assertEquals(blue.x, cross.x, EPSILON, "Face " + face);
            assertEquals(blue.y, cross.y, EPSILON);
            assertEquals(blue.z, cross.z, EPSILON);
        }
    }

    @Test
    void testRedChannelGoesAlongTheCoordinateUOfThePicture() {
        // Moving along the world direction of the red channel moves along +u (and the green channel's direction is -v, the way up the image)
        final var step = 0.01f;
        for (final var face : FACES) {
            final var plane = Math.abs(face.x) > 0.5f ? 0 : Math.abs(face.y) > 0.5f ? 1 : 2;
            final var red = new Vector3f(world(plane, face, new Vector3f(1, 0, 0)));
            final var green = new Vector3f(world(plane, face, new Vector3f(0, 1, 0)));
            final var origin = new Vector3f(3, 4, 5);

            final var start = coordinates(plane, origin, face);
            final var alongRed = coordinates(plane, new Vector3f(origin).fma(step, red), face);
            final var alongGreen = coordinates(plane, new Vector3f(origin).fma(step, green), face);

            assertTrue(alongRed[0] - start[0] > 0.f, "u grows along the red channel, face " + face);
            assertEquals(0.f, alongRed[1] - start[1], 1e-6f, "and v stays");
            assertTrue(alongGreen[1] - start[1] < 0.f, "v falls along the green channel, face " + face);
            assertEquals(0.f, alongGreen[0] - start[0], 1e-6f);
        }
    }

    @Test
    void testPictureOfAWallStandsUp() {
        // The green channel of a normal map on a wall points up in the world
        for (final var wall : new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)}) {
            final var plane = Math.abs(wall.x) > 0.5f ? 0 : 2;
            final var green = world(plane, wall, new Vector3f(0, 1, 0));

            assertEquals(1.f, green[1], EPSILON, "Wall " + wall);
        }
    }
}
