package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Where a point of the water finds its reflection (common/water-reflection.glsl) on the GPU.
 */
final class WaterReflectionShaderTest {
    private static final float EPSILON = 1e-3f;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float[] coordinates(final Matrix4f viewProjection, final Vector3f position, final Vector3f normal, final float distortion) {
        final var uniforms = new HashMap<String, Object>();
        final var numbers = new float[16];
        viewProjection.get(numbers);

        uniforms.put("viewProjection", new ShaderHarness.Matrices(numbers));
        uniforms.put("position", new float[]{position.x, position.y, position.z});
        uniforms.put("normal", new float[]{normal.x, normal.y, normal.z});
        uniforms.put("distortion", distortion);

        return harness.run(List.of("common/water-reflection.glsl"), "uniform mat4 viewProjection; uniform vec3 position; uniform vec3 normal; uniform float distortion;",
                "outputColor = vec4(getReflectionCoordinates(viewProjection, position, normal, distortion), 1.0);", uniforms);
    }

    private static Matrix4f camera() {
        return new Matrix4f().perspective((float) Math.toRadians(60), 1.f, 0.1f, 100.f).lookAt(0.f, 0.f, 0.f, 0.f, 0.f, -1.f, 0.f, 1.f, 0.f);
    }

    @Test
    void testPointInFrontOfTheCameraIsInTheMiddleOfTheImage() {
        final var result = coordinates(camera(), new Vector3f(0.f, 0.f, -10.f), new Vector3f(0.f, 1.f, 0.f), 0.f);

        assertEquals(0.5f, result[0], EPSILON);
        assertEquals(0.5f, result[1], EPSILON);
        assertEquals(1.f, result[2], EPSILON, "Inside of the image the reflection is all there");
    }

    @Test
    void testPointToTheSideIsToTheSide() {
        final var result = coordinates(camera(), new Vector3f(2.f, 1.f, -10.f), new Vector3f(0.f, 1.f, 0.f), 0.f);

        assertTrue(result[0] > 0.5f && result[1] > 0.5f, "Right and up of the middle");
        assertEquals(1.f, result[2], EPSILON);
    }

    @Test
    void testRipplesMoveThePointAlongTheTilt() {
        final var flat = coordinates(camera(), new Vector3f(0.f, 0.f, -10.f), new Vector3f(0.f, 1.f, 0.f), 0.1f);
        final var tilted = coordinates(camera(), new Vector3f(0.f, 0.f, -10.f), new Vector3f(0.2f, 0.9f, -0.1f), 0.1f);

        assertEquals(0.5f, flat[0], EPSILON, "A flat surface doesn't move it");
        assertEquals(0.5f + 0.02f, tilted[0], EPSILON);
        assertEquals(0.5f - 0.01f, tilted[1], EPSILON);
    }

    @Test
    void testReflectionFadesAtTheEdgeOfTheImageAndIsGoneBeyondIt() {
        // The edge of the image at the depth 10 is at y = 5.77 (the field of view is 60 degrees)
        final var inside = coordinates(camera(), new Vector3f(0.f, 5.f, -10.f), new Vector3f(0.f, 1.f, 0.f), 0.f);
        final var near = coordinates(camera(), new Vector3f(0.f, 5.6f, -10.f), new Vector3f(0.f, 1.f, 0.f), 0.f);
        final var beyond = coordinates(camera(), new Vector3f(0.f, 8.f, -10.f), new Vector3f(0.f, 1.f, 0.f), 0.f);

        assertEquals(1.f, inside[2], EPSILON);
        assertTrue(near[2] > 0.f && near[2] < 1.f, "Fading near the edge: " + near[2]);
        assertEquals(0.f, beyond[2], EPSILON, "Nothing beyond the image");
        assertTrue(beyond[1] <= 1.f, "The coordinates are kept in the image");
    }

    @Test
    void testPointBehindTheCameraHasNoReflection() {
        final var result = coordinates(camera(), new Vector3f(0.f, 0.f, 10.f), new Vector3f(0.f, 1.f, 0.f), 0.f);

        assertEquals(0.f, result[2], EPSILON);
    }
}
