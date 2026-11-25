package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_SAMPLE_ALPHA_TO_COVERAGE;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

/**
 * Alpha to coverage for the holes of the cutout surfaces (common/coverage.glsl and the state that the hardware needs).
 */
final class CoverageShaderTest {
    private static final float EPSILON = 1e-4f;
    private static final int SIZE = 8;
    private static final int SAMPLES = 4;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float sharpened(final float alpha, final float cutoff, final float width) {
        return harness.run(List.of("common/coverage.glsl"), "uniform vec3 values;",
                "outputColor = vec4(getSharpenedAlpha(values.x, values.y, values.z), 0.0, 0.0, 1.0);",
                Map.of("values", new float[]{alpha, cutoff, width}))[0];
    }

    @Test
    void testTheLineOfTheCutoffIsHalfCovered() {
        assertEquals(0.5f, sharpened(0.5f, 0.5f, 0.1f), EPSILON);
        assertEquals(0.5f, sharpened(0.3f, 0.3f, 0.02f), EPSILON);
    }

    @Test
    void testHolesAreEmptyAndTheSurfaceIsFull() {
        assertEquals(0.f, sharpened(0.f, 0.5f, 0.1f), EPSILON);
        assertEquals(1.f, sharpened(1.f, 0.5f, 0.1f), EPSILON);
        assertEquals(0.f, sharpened(0.3f, 0.5f, 0.1f), EPSILON, "More than a pixel below the cutoff");
        assertEquals(1.f, sharpened(0.7f, 0.5f, 0.1f), EPSILON, "More than a pixel above it");
    }

    @Test
    void testTheEdgeIsOnePixelWideWhateverTheBlurIs() {
        // A pixel away from the line (the alpha is a width above the cutoff) the surface is full, whether the alpha changes fast or slowly
        assertEquals(1.f, sharpened(0.5f + 0.5f, 0.5f, 0.5f), EPSILON);
        assertEquals(1.f, sharpened(0.5f + 0.01f, 0.5f, 0.01f), EPSILON);
        assertEquals(0.f, sharpened(0.5f - 0.01f, 0.5f, 0.01f), EPSILON);
    }

    @Test
    void testCoverageGrowsWithTheAlpha() {
        var previous = -1.f;
        for (float alpha = 0.4f; alpha <= 0.6f; alpha += 0.02f) {
            final var coverage = sharpened(alpha, 0.5f, 0.2f);

            assertTrue(coverage >= previous, "At " + alpha);
            previous = coverage;
        }
    }

    @Test
    void testAlphaThatDoesNotChangeIsNotDividedByZero() {
        final var inside = sharpened(0.9f, 0.5f, 0.f);
        final var outside = sharpened(0.1f, 0.5f, 0.f);

        assertEquals(1.f, inside, EPSILON);
        assertEquals(0.f, outside, EPSILON);
    }

    /** What a pixel of a multisampled framebuffer has after a triangle of the alpha is drawn, resolved: the share of what is covered */
    private static float coverage(final float alpha, final boolean alphaToCoverage) {
        final var vertex = ShaderResources.expand("post/vertex.glsl");
        final var program = glCreateProgram();
        final var vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertex.code());
        glCompileShader(vertexShader);
        final var fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, "#version 330 core\nuniform float alpha;\nout vec4 outputColor;\nvoid main() { outputColor = vec4(1.0, 1.0, 1.0, alpha); }\n");
        glCompileShader(fragmentShader);
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        assertEquals(GL_TRUE, glGetProgrami(program, GL_LINK_STATUS), glGetProgramInfoLog(program));

        final var multisampled = glGenFramebuffers();
        final var colorBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, colorBuffer);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, SAMPLES, GL_RGBA16F, SIZE, SIZE);
        glBindFramebuffer(GL_FRAMEBUFFER, multisampled);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorBuffer);
        assumeTrue(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE, "The driver has no multisampled framebuffer of floats");

        final var resolved = glGenFramebuffers();
        final var texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, SIZE, SIZE, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glBindFramebuffer(GL_FRAMEBUFFER, resolved);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);

        final var vertexArray = glGenVertexArrays();
        try {
            glBindFramebuffer(GL_FRAMEBUFFER, multisampled);
            glViewport(0, 0, SIZE, SIZE);
            glClearColor(0.f, 0.f, 0.f, 0.f);
            glClear(GL_COLOR_BUFFER_BIT);
            glDisable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);

            if (alphaToCoverage)
                glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE);

            glUseProgram(program);
            glUniform1f(glGetUniformLocation(program, "alpha"), alpha);
            glBindVertexArray(vertexArray);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glBindVertexArray(0);
            glUseProgram(0);
            glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE);

            glBindFramebuffer(GL_READ_FRAMEBUFFER, multisampled);
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, resolved);
            glBlitFramebuffer(0, 0, SIZE, SIZE, 0, 0, SIZE, SIZE, GL_COLOR_BUFFER_BIT, GL_NEAREST);

            glBindFramebuffer(GL_READ_FRAMEBUFFER, resolved);
            final var pixel = MemoryUtil.memAllocFloat(4);
            try {
                glReadPixels(SIZE / 2, SIZE / 2, 1, 1, GL_RGBA, GL_FLOAT, pixel);
                return pixel.get(0);
            } finally {
                MemoryUtil.memFree(pixel);
            }
        } finally {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteVertexArrays(vertexArray);
            glDeleteFramebuffers(multisampled);
            glDeleteFramebuffers(resolved);
            glDeleteRenderbuffers(colorBuffer);
            glDeleteTextures(texture);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            glDeleteProgram(program);
        }
    }

    @Test
    void testAlphaIsTheShareOfTheCoveredSamples() {
        final var half = coverage(0.5f, true);
        assumeTrue(half > 0.f && half < 1.f, "The driver doesn't support alpha to coverage: " + half);

        assertEquals(0.5f, half, 0.26f, "Some of the samples, not all and not none");
        assertEquals(0.f, coverage(0.f, true), EPSILON, "A hole covers nothing");
        assertEquals(1.f, coverage(1.f, true), EPSILON, "A solid surface covers everything");
        assertTrue(coverage(0.25f, true) <= coverage(0.75f, true), "The more alpha, the more samples");
    }

    @Test
    void testWithoutAlphaToCoverageAlphaDoesNotChangeTheCoverage() {
        assertEquals(1.f, coverage(0.f, false), EPSILON, "The blending is off: the alpha is not used by anything");
        assertEquals(1.f, coverage(0.5f, false), EPSILON);
    }
}
