package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.engine.enums.AntialiasingType;
import io.github.trimax.venta.engine.enums.FxaaQuality;
import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.model.common.shared.Antialiasing;
import io.github.trimax.venta.engine.shaders.GLTestContext;
import io.github.trimax.venta.engine.shaders.TestPrograms;

/**
 * FXAA on the GPU: images with edges go through the pass and the smoothed image is read back.
 */
final class FxaaHelperTest {
    private static final int SIZE = 32;

    private static FxaaHelper helper;

    @BeforeAll
    static void context() throws Exception {
        GLTestContext.require();

        helper = new FxaaHelper(TestMemory.create(), TestPrograms::get);
    }

    @AfterAll
    static void deleteEverything() {
        if (helper == null)
            return;

        helper.release();
        TestPrograms.deleteAll();
    }

    private static Antialiasing settings(final FxaaQuality quality, final float subpixel) {
        return new Antialiasing(AntialiasingType.FXAA, quality, subpixel, Antialiasing.DEFAULT_EDGE_THRESHOLD);
    }

    /** Gray levels of the pixels from the function, 0 to 1, in the red channel (the others are the same) */
    private static float[][] smooth(final BiFunction<Integer, Integer, Float> level, final Antialiasing antialiasing) {
        final var data = MemoryUtil.memAlloc(SIZE * SIZE * 4);
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++) {
                final var value = (byte) Math.round(level.apply(x, y) * 255.f);
                data.put(value).put(value).put(value).put((byte) 255);
            }

        data.flip();

        final var source = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, source);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, SIZE, SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        MemoryUtil.memFree(data);

        final var target = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, target);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, SIZE, SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, target, 0);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));
        glViewport(0, 0, SIZE, SIZE);

        helper.render(source, SIZE, SIZE, antialiasing);

        final var pixels = MemoryUtil.memAlloc(SIZE * SIZE * 4);
        glReadPixels(0, 0, SIZE, SIZE, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

        final var result = new float[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++)
                result[y][x] = (pixels.get((y * SIZE + x) * 4) & 0xFF) / 255.f;

        MemoryUtil.memFree(pixels);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(framebuffer);
        glDeleteTextures(source);
        glDeleteTextures(target);

        return result;
    }

    /** A white half plane under the line y = 0.37 x + 8.2: a slanted edge with steps of pixels */
    private static float slanted(final int x, final int y) {
        return y + 0.5f > 0.37f * (x + 0.5f) + 8.2f ? 1.f : 0.f;
    }

    @Test
    void testFlatImageIsNotChanged() {
        final var result = smooth((x, y) -> 0.4f, settings(FxaaQuality.Medium, 0.75f));

        for (final var row : result)
            for (final var value : row)
                assertEquals(0.4f, value, 1.f / 255.f);
    }

    @Test
    void testStaircaseOfASlantedEdgeGetsInBetweenValues() {
        final var result = smooth(FxaaHelperTest::slanted, settings(FxaaQuality.Medium, 0.75f));

        var between = 0;
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++)
                if (result[y][x] > 0.1f && result[y][x] < 0.9f)
                    between++;

        assertTrue(between >= SIZE / 2, "The pixels along the edge got in between values: " + between);
    }

    @Test
    void testPixelsFarFromTheEdgesAreNotChanged() {
        final var result = smooth(FxaaHelperTest::slanted, settings(FxaaQuality.Medium, 0.75f));

        assertEquals(0.f, result[1][SIZE - 3], 1e-3f, "Black far under the edge");
        assertEquals(1.f, result[SIZE - 2][2], 1e-3f, "White far over the edge");
    }

    @Test
    void testSmoothingNeitherBrightensNorDarkensTheImage() {
        final var result = smooth(FxaaHelperTest::slanted, settings(FxaaQuality.High, 1.f));

        for (final var row : result)
            for (final var value : row)
                assertTrue(value >= 0.f && value <= 1.f, "Between the black and the white of the image: " + value);
    }

    @Test
    void testThinLineIsSmoothedMoreWithSubpixel() {
        final BiFunction<Integer, Integer, Float> line = (x, y) -> x == SIZE / 2 ? 1.f : 0.f;

        final var none = smooth(line, settings(FxaaQuality.Medium, 0.f));
        final var full = smooth(line, settings(FxaaQuality.Medium, 1.f));

        assertTrue(Math.abs(none[SIZE / 2][SIZE / 2] - full[SIZE / 2][SIZE / 2]) + Math.abs(none[SIZE / 2][SIZE / 2 + 1] - full[SIZE / 2][SIZE / 2 + 1]) > 0.05f,
                "The line of one pixel is blurred more with the subpixel smoothing");
    }

    @Test
    void testLowContrastIsNotAnEdge() {
        final var result = smooth((x, y) -> x < SIZE / 2 ? 0.50f : 0.51f, settings(FxaaQuality.Medium, 0.75f));

        assertEquals(0.50f, result[SIZE / 2][SIZE / 2 - 1], 1.5f / 255.f);
        assertEquals(0.51f, result[SIZE / 2][SIZE / 2], 1.5f / 255.f);
    }

    @Test
    void testMoreStepsFollowALongerEdgeToItsEnd() {
        // The edge is a long ramp of steps: the low quality gives up before the end of it
        final BiFunction<Integer, Integer, Float> ramp = (x, y) -> y + 0.5f > 0.06f * (x + 0.5f) + 12.f ? 1.f : 0.f;

        final var low = smooth(ramp, settings(FxaaQuality.Low, 0.f));
        final var high = smooth(ramp, settings(FxaaQuality.High, 0.f));

        var difference = 0.f;
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++)
                difference += Math.abs(low[y][x] - high[y][x]);

        assertTrue(difference > 0.f, "The qualities don't give the same image of a long shallow edge");
    }

    @Test
    void testStateOfOpenGlIsRestored() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        smooth(FxaaHelperTest::slanted, settings(FxaaQuality.Medium, 0.75f));

        assertTrue(glIsEnabled(GL_DEPTH_TEST));
        assertFalse(glIsEnabled(GL_BLEND));
    }
}
