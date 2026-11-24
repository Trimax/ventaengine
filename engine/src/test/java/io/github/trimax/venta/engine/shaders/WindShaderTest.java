package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The wind of the shaders (common/wind.glsl) on the GPU. A mesh is a vertical stick from the height 0 to 10, a vertex
 * is moved by the offset the function returns. The time is 0, so the mesh at the origin leans 0.65 of the strength.
 */
final class WindShaderTest {
    private static final float EPSILON = 1e-4f;
    private static final float LEAN = 0.65f;
    private static final float STRENGTH = 2.f;

    private static final float[] IDENTITY = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};

    /** Turned a quarter around z: the local x of the mesh is the height in the world */
    private static final float[] LYING = {0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float[] translation(final float x, final float z) {
        final var matrix = IDENTITY.clone();
        matrix[12] = x;
        matrix[14] = z;
        return matrix;
    }

    /** How far the wind moves a vertex of the mesh; the box of the mesh is the stick unless another one is given */
    private static float[] offset(final float[] model, final float[] vertex, final Map<String, Object> changes) {
        final Map<String, Object> uniforms = new HashMap<>(Map.of(
                "windDirection", new float[]{1, 0},
                "windStrength", STRENGTH,
                "windSpeed", 1.f,
                "windInfluence", 1.f,
                "meshMinimum", new float[]{0, 0, 0},
                "meshMaximum", new float[]{0, 10, 0},
                "timeElapsed", 0.f,
                "testModel", new ShaderHarness.Matrices(model),
                "testVertex", vertex));
        uniforms.putAll(changes);

        final var result = harness.run(List.of("common/wind.glsl"),
                "uniform mat4 testModel;\nuniform vec3 testVertex;",
                "vec3 world = (testModel * vec4(testVertex, 1.0)).xyz;\noutputColor = vec4(applyWind(world, testModel) - world, 1.0);",
                uniforms);

        return new float[]{result[0], result[1], result[2]};
    }

    private static float[] offset(final float height) {
        return offset(IDENTITY, new float[]{0, height, 0}, Map.of());
    }

    @Test
    void testTheTopLeansAlongTheWind() {
        final var moved = offset(10.f);

        assertEquals(LEAN * STRENGTH, moved[0], EPSILON, "The wind blows along x");
        assertEquals(0.f, moved[1], EPSILON, "The wind blows along the ground");
        assertEquals(0.f, moved[2], EPSILON);
    }

    @Test
    void testTheBottomStaysAndTheHigherThePointTheMoreItBends() {
        assertEquals(0.f, offset(0.f)[0], EPSILON, "The bottom of the mesh is where it was");

        final var low = offset(2.f)[0];
        final var middle = offset(5.f)[0];
        final var top = offset(10.f)[0];
        assertTrue(0.f < low && low < middle && middle < top, "Bending grows with the height: " + low + " " + middle + " " + top);
    }

    @Test
    void testAPointAboveTheMeshIsMovedAsTheTopOfIt() {
        assertEquals(offset(10.f)[0], offset(25.f)[0], EPSILON);
        assertEquals(0.f, offset(-3.f)[0], EPSILON);
    }

    @Test
    void testAMeshThatDoesNotFollowTheWindStays() {
        final var moved = offset(IDENTITY, new float[]{0, 10, 0}, Map.of("windInfluence", 0.f));

        assertArrayEquals(new float[]{0, 0, 0}, moved, EPSILON);
    }

    @Test
    void testASceneWithoutWindIsACalm() {
        final var moved = offset(IDENTITY, new float[]{0, 10, 0}, Map.of("windStrength", 0.f));

        assertArrayEquals(new float[]{0, 0, 0}, moved, EPSILON);
    }

    @Test
    void testInfluenceAndStrengthScaleTheBending() {
        assertEquals(LEAN * STRENGTH * 0.5f, offset(IDENTITY, new float[]{0, 10, 0}, Map.of("windInfluence", 0.5f))[0], EPSILON);
        assertEquals(LEAN * 3.f, offset(IDENTITY, new float[]{0, 10, 0}, Map.of("windStrength", 3.f))[0], EPSILON);
    }

    @Test
    void testTheDirectionOfTheWindDecidesWhereTheTopLeans() {
        final var moved = offset(IDENTITY, new float[]{0, 10, 0}, Map.of("windDirection", new float[]{0, 1}));

        assertEquals(0.f, moved[0], EPSILON);
        assertEquals(LEAN * STRENGTH, moved[2], EPSILON, "The wind blows along z");
    }

    @Test
    void testTheHeightIsTheHeightInTheWorldWhateverWayTheMeshIsTurned() {
        // The box of the mesh is a stick along x, lying down it is vertical: its end is the top
        final var box = Map.<String, Object>of("meshMaximum", new float[]{10, 0, 0});

        assertEquals(LEAN * STRENGTH, offset(LYING, new float[]{10, 0, 0}, box)[0], EPSILON, "The end that is up bends");
        assertEquals(0.f, offset(LYING, new float[]{0, 0, 0}, box)[0], EPSILON, "The end that is down stays");
    }

    @Test
    void testCopiesOfAMeshAreOutOfStep() {
        final var vertex = new float[]{0, 10, 0};
        final var time = Map.<String, Object>of("timeElapsed", 1.7f);

        final var first = offset(IDENTITY, vertex, time);
        final var second = offset(translation(9.f, 4.f), vertex, time);

        assertTrue(Math.abs(first[0] - second[0]) > 0.01f, "Two trees at different places sway differently: " + first[0] + " " + second[0]);
    }

    @Test
    void testGustsMoveTheTopButNeverAgainstTheWind() {
        final var vertex = new float[]{0, 10, 0};
        var smallest = Float.MAX_VALUE;
        var largest = -Float.MAX_VALUE;
        for (int step = 0; step < 100; step++) {
            final var moved = offset(IDENTITY, vertex, Map.of("timeElapsed", step * 0.37f))[0];
            smallest = Math.min(smallest, moved);
            largest = Math.max(largest, moved);
        }

        assertTrue(smallest > 0.f, "The tree is never pushed against the wind: " + smallest);
        assertTrue(largest - smallest > 0.3f, "The gusts swing the tree: " + smallest + " to " + largest);
        assertTrue(largest <= STRENGTH * 1.01f, "The top never leans further than the strength: " + largest);
    }

    @Test
    void testASurfaceAndItsShadowUseTheSameWind() {
        for (final var shader : List.of("default/vertex.glsl", "default/vertex-instanced.glsl", "shadow/vertex.glsl",
                "shadow/cutout-vertex.glsl", "shadow/vertex-instanced.glsl", "shadow/cutout-vertex-instanced.glsl")) {
            final var code = ShaderResources.expand(shader).code();

            assertTrue(code.contains("applyWind("), shader + " sways with the wind");
            assertEquals(1, code.split("vec3 applyWind\\(", -1).length - 1, shader + " has one wind function");
        }
    }
}
