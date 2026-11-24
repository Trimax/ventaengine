package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.joml.Matrix4f;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

/**
 * The ambient occlusion (common/ambient-occlusion.glsl) on the GPU: the functions that decide how much a sample darkens a
 * point, and the whole pass over the depth of a made up scene (a floor and a block that stands on it, seen from above).
 */
final class AmbientOcclusionShaderTest {
    private static final float EPSILON = 1e-4f;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @AfterAll
    static void deleteEverything() {
        TestPrograms.deleteAll();
    }

    private static float sampleOcclusion(final float[] center, final float[] normal, final float[] sampled, final float radius) {
        return harness.run(List.of("common/ambient-occlusion.glsl"),
                "uniform vec3 center; uniform vec3 normal; uniform vec3 sampled; uniform float radius;",
                "outputColor = vec4(getAoSampleOcclusion(center, normal, sampled, radius));",
                Map.of("center", center, "normal", normal, "sampled", sampled, "radius", radius))[0];
    }

    private static float[] normal(final float[] center, final float[] right, final float[] left, final float[] up, final float[] down, final float[] eye) {
        final var result = harness.run(List.of("common/ambient-occlusion.glsl"),
                "uniform vec3 center; uniform vec3 right; uniform vec3 left; uniform vec3 up; uniform vec3 down; uniform vec3 eye;",
                "outputColor = vec4(getAoNormal(center, right, left, up, down, eye), 1.0);",
                Map.of("center", center, "right", right, "left", left, "up", up, "down", down, "eye", eye));

        return new float[]{result[0], result[1], result[2]};
    }

    @Test
    void testSampleAboveTheSurfaceDarkensAndOneInItDoesNot() {
        final var center = new float[]{0, 0, 0};
        final var up = new float[]{0, 0, 1};

        assertEquals(0.f, sampleOcclusion(center, up, new float[]{0.5f, 0, 0}, 2.f), EPSILON, "In the plane of the surface");
        assertEquals(0.f, sampleOcclusion(center, up, new float[]{0.5f, 0, -0.5f}, 2.f), EPSILON, "Below the surface");
        assertTrue(sampleOcclusion(center, up, new float[]{0.5f, 0, 1.f}, 2.f) > 0.1f, "Above the surface and near");
    }

    @Test
    void testSampleBeyondTheRadiusDarkensNothing() {
        assertEquals(0.f, sampleOcclusion(new float[]{0, 0, 0}, new float[]{0, 0, 1}, new float[]{0, 0, 2.5f}, 2.f), EPSILON);
        assertEquals(0.f, sampleOcclusion(new float[]{0, 0, 0}, new float[]{0, 0, 1}, new float[]{0, 0, 2.f}, 2.f), EPSILON, "At the radius");
    }

    @Test
    void testNearerAndHigherSamplesDarkenMore() {
        final var center = new float[]{0, 0, 0};
        final var up = new float[]{0, 0, 1};

        final var near = sampleOcclusion(center, up, new float[]{0.3f, 0, 0.5f}, 3.f);
        final var far = sampleOcclusion(center, up, new float[]{1.2f, 0, 0.5f}, 3.f);
        final var high = sampleOcclusion(center, up, new float[]{1.2f, 0, 1.f}, 3.f);

        assertTrue(near > far, "Nearer: " + near + " against " + far);
        assertTrue(high > far, "Higher: " + high + " against " + far);
    }

    @Test
    void testOcclusionOfASampleIsFromZeroToOne() {
        for (final var z : new float[]{0.001f, 0.01f, 0.1f, 1.f})
            for (final var x : new float[]{0.f, 0.001f, 0.5f, 1.5f}) {
                final var value = sampleOcclusion(new float[]{0, 0, 0}, new float[]{0, 0, 1}, new float[]{x, 0, z}, 2.f);

                assertTrue(value >= 0.f && value <= 1.f, "At " + x + ", " + z + ": " + value);
            }
    }

    @Test
    void testNormalOfAFlatSurfaceFacesTheEye() {
        final var result = normal(new float[]{0, 0, 0}, new float[]{1, 0, 0}, new float[]{-1, 0, 0}, new float[]{0, 1, 0}, new float[]{0, -1, 0}, new float[]{0, 0, 5});

        assertArrayEquals(new float[]{0, 0, 1}, result, EPSILON);
    }

    @Test
    void testNormalOfASlopeLeansTowardsTheWayItFalls() {
        // A surface that rises along x as z = x: its normal is (-1, 0, 1) / sqrt(2)
        final var result = normal(new float[]{0, 0, 0}, new float[]{1, 0, 1}, new float[]{-1, 0, -1}, new float[]{0, 1, 0}, new float[]{0, -1, 0}, new float[]{0, 0, 5});

        final var root = (float) Math.sqrt(0.5);
        assertArrayEquals(new float[]{-root, 0, root}, result, EPSILON);
    }

    @Test
    void testNormalAtAnEdgeIsTheOneOfTheNearSurface() {
        // The right neighbor is on another surface far behind: it is not taken, the left one is
        final var result = normal(new float[]{0, 0, 0}, new float[]{1, 0, -20}, new float[]{-1, 0, 0}, new float[]{0, 1, 0}, new float[]{0, -1, 0}, new float[]{0, 0, 5});

        assertArrayEquals(new float[]{0, 0, 1}, result, EPSILON, "The jump to the surface behind is not the surface");
    }

    @Test
    void testSampleWithTheSameDistanceCountsMore() {
        final var same = weight(100.f, 100.f);
        final var near = weight(100.f, 97.f);
        final var far = weight(100.f, 20.f);

        assertTrue(same > near && near > far && far > 0.f, same + " " + near + " " + far);
        assertEquals(weight(100.f, 70.f), weight(70.f, 100.f), 0.f, "The order doesn't matter");
    }

    private static float weight(final float eyeDistance, final float sampled) {
        return harness.run(List.of("common/ambient-occlusion.glsl"), "uniform float eyeDistance; uniform float sampled;",
                "outputColor = vec4(getAoWeight(eyeDistance, sampled));", Map.of("eyeDistance", eyeDistance, "sampled", sampled))[0];
    }

    /**
     * Runs the pass of the estimate over the depth of a made up scene. The camera looks down along -z with an orthographic
     * projection of 32 by 32 units, and the depth is a function of the pixel: the pass writes an image of 16 by 16.
     */
    private static float[][] estimate(final BiFunction<Integer, Integer, Float> depth) {
        final var size = 32;
        final var half = size / 2;

        final FloatBuffer data = MemoryUtil.memAllocFloat(size * size);
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                data.put(depth.apply(x, y));

        data.flip();

        final var texture = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, size, size, 0, GL_DEPTH_COMPONENT, GL_FLOAT, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        MemoryUtil.memFree(data);

        final var target = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, target);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, half, half, 0, GL_RG, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, target, 0);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));

        final var vertexArray = glGenVertexArrays();
        final var program = TestPrograms.get("ambient-occlusion.json");
        final var inverse = new Matrix4f().ortho(-16.f, 16.f, -16.f, 16.f, 0.f, 10.f).invert();

        try {
            // The unit 0 was used to make the image the pass draws into: the depth goes back to it
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);

            glUseProgram(program);
            glUniform1i(glGetUniformLocation(program, "textureDepth"), 0);
            final var matrix = MemoryUtil.memAllocFloat(16);
            glUniformMatrix4fv(glGetUniformLocation(program, "matrixViewProjectionInverse"), false, inverse.get(matrix));
            MemoryUtil.memFree(matrix);
            glUniform3f(glGetUniformLocation(program, "cameraPosition"), 0.f, 0.f, 0.f);
            glUniform1f(glGetUniformLocation(program, "aoRadius"), 8.f);
            glUniform1f(glGetUniformLocation(program, "aoPower"), 1.f);

            // A unit of the world is a pixel in this projection, and the surface is about 5 units away: the radius of 8 units is 8 pixels
            glUniform1f(glGetUniformLocation(program, "aoProjectionScale"), 5.2f);

            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            glViewport(0, 0, half, half);
            glBindVertexArray(vertexArray);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glBindVertexArray(0);

            final var pixels = MemoryUtil.memAllocFloat(half * half * 4);
            glReadPixels(0, 0, half, half, GL_RGBA, GL_FLOAT, pixels);

            final var result = new float[half][half];
            for (int y = 0; y < half; y++)
                for (int x = 0; x < half; x++)
                    result[y][x] = pixels.get((y * half + x) * 4);

            MemoryUtil.memFree(pixels);
            return result;
        } finally {
            glUseProgram(0);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(framebuffer);
            glDeleteVertexArrays(vertexArray);
            glDeleteTextures(target);
            glDeleteTextures(texture);
        }
    }

    @Test
    void testFlatFloorIsNotOccluded() {
        final var result = estimate((x, y) -> 0.5f);

        for (int y = 2; y < 14; y++)
            for (int x = 2; x < 14; x++)
                assertEquals(1.f, result[y][x], 0.02f, "At " + x + ", " + y);
    }

    @Test
    void testFloorNextToABlockIsDarkerThanFarFromIt() {
        // The block fills the right half of the image and rises 2 units (0.2 of the depth range of 10) above the floor
        final var result = estimate((x, y) -> x >= 16 ? 0.3f : 0.5f);

        final var row = 8;
        final var atTheBlock = result[row][7];
        final var farFromIt = result[row][1];

        assertTrue(atTheBlock < 0.9f, "The floor at the foot of the block is darkened: " + atTheBlock);
        assertTrue(farFromIt > 0.97f, "The floor far from it is not: " + farFromIt);
        assertTrue(result[row][5] >= atTheBlock, "The occlusion fades away from the foot: " + result[row][5] + " against " + atTheBlock);
    }

    @Test
    void testTopOfTheBlockIsNotOccluded() {
        final var result = estimate((x, y) -> x >= 16 ? 0.3f : 0.5f);

        assertEquals(1.f, result[8][9], 0.03f, "The top of a block has nothing above it, the floor below doesn't darken it");
    }

    @Test
    void testSkyIsNotOccluded() {
        final var result = estimate((x, y) -> 1.f);

        assertEquals(1.f, result[8][8], EPSILON);
    }

    @Test
    void testTheMoreThereIsAroundAPointTheDarkerItIs() {
        // A wide block and a thin strip of the same height next to the same floor: more of the samples are on the wide one
        final var wide = estimate((x, y) -> x >= 16 ? 0.3f : 0.5f)[8][7];
        final var thin = estimate((x, y) -> x >= 16 && x < 18 ? 0.3f : 0.5f)[8][7];

        assertTrue(wide < thin, "A wide block darkens more than a thin strip: " + wide + " against " + thin);
    }
}
