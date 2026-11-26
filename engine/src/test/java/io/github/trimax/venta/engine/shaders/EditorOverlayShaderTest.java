package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * The shaders of the overlay of an editor (the lines and the grid) on the GPU: a small image is drawn and its pixels are read.
 */
final class EditorOverlayShaderTest {
    private static final int SIZE = 64;

    private static int target;
    private static int framebuffer;
    private static int depth;
    private static int depthFramebuffer;
    private static int vertexArray;

    @BeforeAll
    static void context() {
        GLTestContext.require();

        target = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, target);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, SIZE, SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, target, 0);

        // The depth of the scene is a texture that the shaders read: a float one is the same for them
        depth = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depth);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, SIZE, SIZE, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        depthFramebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, depthFramebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, depth, 0);

        vertexArray = glGenVertexArrays();
    }

    @AfterAll
    static void cleanup() {
        TestPrograms.deleteAll();
    }

    /** What the scene has drawn: its depth is the same everywhere */
    private static void fillDepth(final float value) {
        glBindFramebuffer(GL_FRAMEBUFFER, depthFramebuffer);
        glViewport(0, 0, SIZE, SIZE);
        try (final var stack = MemoryStack.stackPush()) {
            glClearBufferfv(GL_COLOR, 0, stack.floats(value, 0.f, 0.f, 0.f));
        }
    }

    private static void begin() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(0, 0, SIZE, SIZE);
        glClearColor(0.f, 0.f, 0.f, 0.f);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depth);
        glBindVertexArray(vertexArray);
    }

    private static float[][] read() {
        glBindVertexArray(0);
        glUseProgram(0);

        final var pixels = MemoryUtil.memAllocFloat(SIZE * SIZE * 4);
        try {
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
            glReadPixels(0, 0, SIZE, SIZE, GL_RGBA, GL_FLOAT, pixels);

            final var image = new float[SIZE * SIZE][4];
            for (int index = 0; index < SIZE * SIZE; index++)
                for (int channel = 0; channel < 4; channel++)
                    image[index][channel] = pixels.get(index * 4 + channel);

            return image;
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }

    private static float[] pixel(final float[][] image, final int x, final int y) {
        return image[y * SIZE + x];
    }

    /** Draws segments with the lines program: the view is the identity, the camera looks along -z with a 90 degree field */
    private static float[][] lines(final float[] starts, final float[] ends, final float[] colors, final int count) {
        final var program = TestPrograms.get("editor-lines.json");
        begin();
        glUseProgram(program);

        try (final var stack = MemoryStack.stackPush()) {
            final FloatBuffer matrix = stack.mallocFloat(16);
            glUniformMatrix4fv(glGetUniformLocation(program, "viewMatrix"), false, new Matrix4f().get(matrix));
            glUniformMatrix4fv(glGetUniformLocation(program, "projectionMatrix"), false, new Matrix4f().perspective((float) Math.toRadians(90.0), 1.f, 0.1f, 100.f).get(matrix));
        }

        glUniform2f(glGetUniformLocation(program, "viewportSize"), SIZE, SIZE);
        glUniform1f(glGetUniformLocation(program, "thickness"), 4.f);
        glUniform1f(glGetUniformLocation(program, "nearPlane"), 0.1f);
        glUniform1i(glGetUniformLocation(program, "textureDepth"), 0);
        glUniform3fv(glGetUniformLocation(program, "segmentStart"), starts);
        glUniform3fv(glGetUniformLocation(program, "segmentEnd"), ends);
        glUniform4fv(glGetUniformLocation(program, "segmentColor"), colors);
        glDrawArrays(GL_TRIANGLES, 0, count * 6);

        return read();
    }

    /** One horizontal segment at the middle of the picture: from x = -1 to 1 at 2 meters, which is the middle half of the picture */
    private static float[][] horizontalLine() {
        return lines(new float[]{-1.f, 0.f, -2.f}, new float[]{1.f, 0.f, -2.f}, new float[]{1.f, 0.5f, 0.25f, 1.f}, 1);
    }

    @Test
    void testLineIsDrawnWithItsColorAndWidth() {
        fillDepth(1.f);
        final var image = horizontalLine();

        final var middle = pixel(image, SIZE / 2, SIZE / 2);
        assertEquals(1.f, middle[0], 0.02f);
        assertEquals(0.5f, middle[1], 0.02f);
        assertEquals(1.f, middle[3], 0.02f, "Nothing is in front of it");

        assertEquals(0.f, pixel(image, SIZE / 2, SIZE / 2 + 5)[3], "A line of 4 pixels is not 10 pixels wide");
        assertEquals(0.f, pixel(image, SIZE / 2, SIZE / 2 - 5)[3]);
    }

    @Test
    void testLineEndsWhereTheSegmentEnds() {
        fillDepth(1.f);
        final var image = horizontalLine();

        assertTrue(pixel(image, 20, SIZE / 2)[3] > 0.9f, "The segment goes from the quarter of the picture");
        assertEquals(0.f, pixel(image, 8, SIZE / 2)[3], "Not farther than its end");
        assertEquals(0.f, pixel(image, SIZE - 8, SIZE / 2)[3]);
    }

    @Test
    void testLineBehindWhatTheSceneDrewIsFaint() {
        fillDepth(0.f);
        final var image = horizontalLine();

        final var alpha = pixel(image, SIZE / 2, SIZE / 2)[3];
        assertTrue(alpha > 0.1f && alpha < 0.5f, "It is seen faintly: " + alpha);
    }

    @Test
    void testSegmentBehindTheCameraIsNotDrawn() {
        fillDepth(1.f);
        final var image = lines(new float[]{-1.f, 0.f, 2.f}, new float[]{1.f, 0.f, 3.f}, new float[]{1.f, 1.f, 1.f, 1.f}, 1);

        for (final var pixel : image)
            assertEquals(0.f, pixel[3]);
    }

    @Test
    void testSegmentThatReachesBehindTheCameraIsCutAtTheNearPlane() {
        fillDepth(1.f);
        final var image = lines(new float[]{-1.f, 0.f, -2.f}, new float[]{-1.f, 0.f, 5.f}, new float[]{1.f, 1.f, 1.f, 1.f}, 1);

        // The segment starts at the quarter of the picture and goes to the left, to the near plane far outside of the picture
        assertTrue(pixel(image, 8, SIZE / 2)[3] > 0.5f, "The part in front of the camera is drawn, it goes to the edge of the picture");
        assertEquals(0.f, pixel(image, SIZE - 10, SIZE / 2)[3]);
        for (final var pixel : image)
            assertFalse(Float.isNaN(pixel[3]));
    }

    @Test
    void testEverySegmentOfTheListIsDrawn() {
        fillDepth(1.f);
        final var image = lines(new float[]{-1.f, 0.f, -2.f, -1.f, 0.5f, -2.f}, new float[]{1.f, 0.f, -2.f, 1.f, 0.5f, -2.f},
                new float[]{1.f, 0.f, 0.f, 1.f, 0.f, 1.f, 0.f, 1.f}, 2);

        assertEquals(1.f, pixel(image, SIZE / 2, SIZE / 2)[0], 0.02f, "The first one is red");
        assertEquals(1.f, pixel(image, SIZE / 2, SIZE / 2 + 8)[1], 0.02f, "The second one is a quarter of the picture higher: green");
    }

    /** The camera is 5 meters over the ground and looks down (or up), the top of the picture is -z */
    private static float[][] grid(final boolean down) {
        final var view = new Matrix4f().lookAt(0.f, 5.f, 0.f, 0.f, down ? 0.f : 10.f, 0.f, 0.f, 0.f, down ? -1.f : 1.f);
        final var projection = new Matrix4f().perspective((float) Math.toRadians(60.0), 1.f, 0.1f, 100.f);

        final var program = TestPrograms.get("editor-grid.json");
        begin();
        glUseProgram(program);

        try (final var stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(glGetUniformLocation(program, "inverseViewProjection"), false, new Matrix4f(projection).mul(view).invert().get(stack.mallocFloat(16)));
        }

        glUniform3f(glGetUniformLocation(program, "cameraPosition"), 0.f, 5.f, 0.f);
        glUniform1f(glGetUniformLocation(program, "fadeDistance"), 400.f);
        glUniform1i(glGetUniformLocation(program, "textureDepth"), 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);

        return read();
    }

    @Test
    void testGridHasTheAxesAtTheOriginAndLinesEveryMeter() {
        fillDepth(1.f);
        final var image = grid(true);

        assertTrue(pixel(image, SIZE / 2, SIZE / 2)[3] > 0.5f, "The axes cross under the camera");

        // A row away from the axes: the lines of the meters cross it, between them is the ground
        var lines = 0;
        for (int x = 0; x < SIZE; x++)
            if (pixel(image, x, 8)[3] > 0.05f)
                lines++;

        assertTrue(lines >= 2 && lines < SIZE / 2, "Thin lines, not a filled row: " + lines);
    }

    @Test
    void testGridIsNotInTheSky() {
        fillDepth(1.f);
        final var image = grid(false);

        for (final var pixel : image)
            assertEquals(0.f, pixel[3], "A camera that looks up sees no ground");
    }

    @Test
    void testWhatTheSceneDrewInFrontHidesTheGrid() {
        fillDepth(0.f);
        final var image = grid(true);

        for (final var pixel : image)
            assertEquals(0.f, pixel[3]);
    }
}
