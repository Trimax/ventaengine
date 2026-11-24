package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.model.common.post.BloomTargets;
import io.github.trimax.venta.engine.model.common.shared.Bloom;
import io.github.trimax.venta.engine.shaders.GLTestContext;
import io.github.trimax.venta.engine.shaders.TestPrograms;

/**
 * The glow of an image on the GPU: images of known colors go through the chain and the first level is read back.
 */
final class BloomHelperTest {
    private static final int SIZE = 64;
    private static final float TOLERANCE = 0.05f;

    private static BloomHelper helper;

    @BeforeAll
    static void context() throws Exception {
        GLTestContext.require();

        helper = new BloomHelper(TestMemory.create(), TestPrograms::get);
    }

    @AfterAll
    static void deleteEverything() {
        if (helper == null)
            return;

        helper.release();
        TestPrograms.deleteAll();
    }

    /** An image of the size 64 by 64 where every pixel has the color the function gives */
    private static int image(final BiFunction<Integer, Integer, float[]> color) {
        final FloatBuffer data = MemoryUtil.memAllocFloat(SIZE * SIZE * 4);
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++)
                data.put(color.apply(x, y)).put(1.f);

        data.flip();

        final var id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, SIZE, SIZE, 0, GL_RGBA, GL_FLOAT, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        MemoryUtil.memFree(data);

        return id;
    }

    /** The pixels of the first level as red values: the glow is white or gray in these tests */
    private static float[][] read(final int textureID) {
        final var size = SIZE / 2;
        final FloatBuffer data = MemoryUtil.memAllocFloat(size * size * 4);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_FLOAT, data);
        glBindTexture(GL_TEXTURE_2D, 0);

        final var result = new float[size][size];
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                result[y][x] = data.get((y * size + x) * 4);

        MemoryUtil.memFree(data);
        return result;
    }

    private static float[][] glow(final BiFunction<Integer, Integer, float[]> color, final Bloom bloom) {
        final var source = image(color);
        final var texture = helper.render(source, SIZE, SIZE, bloom);
        final var result = read(texture);

        glDeleteTextures(source);
        return result;
    }

    @Test
    void testImageBelowTheThresholdHasNoGlow() {
        final var result = glow((x, y) -> new float[]{0.5f, 0.5f, 0.5f}, new Bloom(0.1f, 1.f, 0.f));

        for (final var row : result)
            for (final var value : row)
                assertEquals(0.f, value, 1e-4f, "Nothing is bright enough");
    }

    @Test
    void testBrightPixelGlowsAroundItself() {
        final var result = glow((x, y) -> x == SIZE / 2 && y == SIZE / 2 ? new float[]{200.f, 200.f, 200.f} : new float[]{0.f, 0.f, 0.f},
                new Bloom(0.1f, 1.f, 0.5f));

        final var center = result[SIZE / 4][SIZE / 4];
        assertTrue(center > 0.1f, "The glow is where the pixel is: " + center);
        assertTrue(result[SIZE / 4][SIZE / 4 + 3] > 0.f, "It spreads to the neighbors");
        assertTrue(result[SIZE / 4][SIZE / 4 + 3] < center, "The farther, the weaker");
        assertTrue(result[SIZE / 4][SIZE / 4 + 8] < result[SIZE / 4][SIZE / 4 + 3], "It fades with the distance");
        assertEquals(0.f, result[1][1], 0.02f, "Far from it there is almost nothing");
    }

    @Test
    void testGlowOfASpotIsSymmetric() {
        // Four pixels around the point (32, 32), which is the corner of four texels of the first level: (15, 15) to (16, 16)
        final var result = glow((x, y) -> (x == SIZE / 2 || x == SIZE / 2 - 1) && (y == SIZE / 2 || y == SIZE / 2 - 1)
                        ? new float[]{100.f, 100.f, 100.f} : new float[]{0.f, 0.f, 0.f},
                new Bloom(0.1f, 1.f, 0.5f));

        assertEquals(result[15][15], result[16][16], 1e-3f * result[15][15] + 1e-4f);
        assertEquals(result[15][16], result[16][15], 1e-3f * result[15][16] + 1e-4f);
        assertEquals(result[15][15], result[15][16], 1e-3f * result[15][15] + 1e-4f);
    }

    @Test
    void testUniformBrightImageGlowsAsWholeFromEveryScale() {
        // The excess over the threshold is 2 and every one of the levels adds it once
        final var levels = BloomTargets.getLevelCount(SIZE, SIZE);
        final var result = glow((x, y) -> new float[]{3.f, 3.f, 3.f}, new Bloom(0.1f, 1.f, 0.5f));

        final var expected = 2.f * levels;
        assertEquals(expected, result[SIZE / 4][SIZE / 4], expected * TOLERANCE, "The middle");
        assertEquals(expected, result[1][1], expected * TOLERANCE, "The corner: the edges are clamped, so the glow doesn't fade there");
    }

    @Test
    void testHigherThresholdLeavesLessGlow() {
        final var color = (BiFunction<Integer, Integer, float[]>) (x, y) -> new float[]{4.f, 4.f, 4.f};

        final var low = glow(color, new Bloom(0.1f, 1.f, 0.f))[SIZE / 4][SIZE / 4];
        final var high = glow(color, new Bloom(0.1f, 3.f, 0.f))[SIZE / 4][SIZE / 4];

        assertTrue(low > high, "The glow is the excess over the threshold: " + low + " against " + high);
        assertTrue(high > 0.f);
    }

    @Test
    void testStateOfOpenGlIsRestored() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(3, 5, 77, 99);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        glow((x, y) -> new float[]{5.f, 5.f, 5.f}, new Bloom(0.1f, 1.f, 0.5f));

        final var viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        assertArrayEquals(new int[]{3, 5, 77, 99}, viewport, "The viewport is the one of the caller");
        assertEquals(0, glGetInteger(GL_FRAMEBUFFER_BINDING), "The framebuffer is the one of the caller");
        assertTrue(glIsEnabled(GL_DEPTH_TEST));
        assertFalse(glIsEnabled(GL_BLEND));
    }

    @Test
    void testImageOfAnotherSizeGetsItsOwnChain() {
        final var small = glow((x, y) -> new float[]{3.f, 3.f, 3.f}, new Bloom(0.1f, 1.f, 0.5f));
        assertEquals(SIZE / 2, small.length);

        final var texture = helper.render(image((x, y) -> new float[]{3.f, 3.f, 3.f}), SIZE, SIZE, new Bloom(0.1f, 1.f, 0.5f));
        assertTrue(glIsTexture(texture));

        final var bigger = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, bigger);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, 128, 96, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);

        final var other = helper.render(bigger, 128, 96, new Bloom(0.1f, 1.f, 0.5f));
        assertTrue(glIsTexture(other), "The chain is made again for the new size");
        glDeleteTextures(bigger);
    }
}
