package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.engine.exceptions.CubemapBakeException;
import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.shaders.GLTestContext;
import io.github.trimax.venta.engine.shaders.TestPrograms;

/**
 * A cube map made of a panorama in HDR, on the GPU: the panorama is a Radiance file that the test writes (4 by 2 pixels, which is
 * stored flat, without the run length encoding).
 */
final class EquirectangularHelperTest {
    private static final int RESOLUTION = 16;

    private static EquirectangularHelper helper;

    @BeforeAll
    static void context() throws Exception {
        GLTestContext.require();

        helper = new EquirectangularHelper(TestMemory.create(), TestPrograms::get);
    }

    @AfterAll
    static void deleteEverything() {
        TestPrograms.deleteAll();
    }

    /** The bytes of a Radiance file of 4 by 2 pixels: the color of a column (the same in both rows) */
    private static byte[] panorama(final float[][] columns) {
        final var output = new ByteArrayOutputStream();
        final var header = "#?RADIANCE\nFORMAT=32-bit_rle_rgbe\n\n-Y 2 +X 4\n";
        output.writeBytes(header.getBytes(StandardCharsets.US_ASCII));

        for (int row = 0; row < 2; row++)
            for (final var color : columns) {
                final var largest = Math.max(color[0], Math.max(color[1], color[2]));
                if (largest < 1e-32f) {
                    output.writeBytes(new byte[]{0, 0, 0, 0});
                    continue;
                }

                // RGBE: the mantissas are scaled by the power of two of the largest channel
                final var exponent = Math.getExponent(largest) + 1;
                final var scale = (float) Math.pow(2.0, -exponent) * 256.f;
                output.write((int) (color[0] * scale));
                output.write((int) (color[1] * scale));
                output.write((int) (color[2] * scale));
                output.write(exponent + 128);
            }

        return output.toByteArray();
    }

    private static byte[] uniform(final float r, final float g, final float b) {
        return panorama(new float[][]{{r, g, b}, {r, g, b}, {r, g, b}, {r, g, b}});
    }

    /** The color of the middle pixel of a face of a cube map */
    private static float[] center(final int cubeID, final int face) {
        final var data = MemoryUtil.memAllocFloat(RESOLUTION * RESOLUTION * 4);
        try {
            glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);
            glGetTexImage(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL_RGBA, GL_FLOAT, data);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

            final var index = ((RESOLUTION / 2) * RESOLUTION + RESOLUTION / 2) * 4;
            return new float[]{data.get(index), data.get(index + 1), data.get(index + 2), data.get(index + 3)};
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    @Test
    void testBrightnessOfThePanoramaIsKeptAboveOne() {
        final var cube = helper.convert(uniform(3.f, 6.f, 1.5f), "bright", RESOLUTION, 1000.f);

        for (int face = 0; face < 6; face++) {
            final var color = center(cube, face);
            assertEquals(3.f, color[0], 3.f * 0.02f, "Red of the face " + face);
            assertEquals(6.f, color[1], 6.f * 0.02f, "Green of the face " + face);
            assertEquals(1.5f, color[2], 1.5f * 0.02f, "Blue of the face " + face);
        }
    }

    @Test
    void testClampKeepsTheBrightestValueOnly() {
        final var cube = helper.convert(uniform(3.f, 6.f, 1.5f), "clamped", RESOLUTION, 2.f);

        final var color = center(cube, 4);
        assertEquals(2.f, color[0], 0.01f);
        assertEquals(2.f, color[1], 0.01f);
        assertEquals(1.5f, color[2], 1.5f * 0.02f, "What is below it stays");
    }

    @Test
    void testFacesLookWhereThePanoramaSays() {
        // The left half of the picture is red, the right half blue: the right is +x (the face 0), the left is -x (the face 1)
        final var cube = helper.convert(panorama(new float[][]{{4.f, 0.f, 0.f}, {4.f, 0.f, 0.f}, {0.f, 0.f, 4.f}, {0.f, 0.f, 4.f}}), "halves", RESOLUTION, 100.f);

        final var plusX = center(cube, 0);
        assertTrue(plusX[2] > 3.f && plusX[0] < 0.5f, "+x is blue: " + java.util.Arrays.toString(plusX));

        final var minusX = center(cube, 1);
        assertTrue(minusX[0] > 3.f && minusX[2] < 0.5f, "-x is red: " + java.util.Arrays.toString(minusX));
    }

    @Test
    void testFacesAtTheBorderOfTwoColorsMixThem() {
        final var cube = helper.convert(panorama(new float[][]{{4.f, 0.f, 0.f}, {4.f, 0.f, 0.f}, {0.f, 0.f, 4.f}, {0.f, 0.f, 4.f}}), "borders", RESOLUTION, 100.f);

        final var minusZ = center(cube, 5);
        assertTrue(minusZ[0] > 1.f && minusZ[2] > 1.f, "The middle of the picture is the border of red and blue: " + java.util.Arrays.toString(minusZ));

        final var plusZ = center(cube, 4);
        assertTrue(plusZ[0] > 1.f && plusZ[2] > 1.f, "The edges of the picture are next to each other, so behind is both: " + java.util.Arrays.toString(plusZ));
    }

    @Test
    void testStateOfOpenGlIsRestored() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(3, 5, 77, 99);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        helper.convert(uniform(1.f, 1.f, 1.f), "state", RESOLUTION, 10.f);

        final var viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        assertArrayEquals(new int[]{3, 5, 77, 99}, viewport);
        assertTrue(glIsEnabled(GL_DEPTH_TEST));
        assertFalse(glIsEnabled(GL_BLEND));
    }

    @Test
    void testFileThatIsNotARadianceFileIsRefused() {
        final var exception = assertThrows(CubemapBakeException.class, () -> helper.convert(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, "broken", RESOLUTION, 10.f));

        assertTrue(exception.getMessage().contains("broken"), "It says which sky: " + exception.getMessage());
    }
}
