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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

/**
 * The shadow of a cutout material on the GPU: a full screen triangle at the depth 0.5 is drawn with the fragment shader
 * of the shadow pass, the depth is written where the surface is not a hole. The texture has two texels: the left one
 * is opaque, the right one is a hole.
 */
final class ShadowCutoutShaderTest {
    private static final int WIDTH = 4;
    private static final float EPSILON = 1e-5f;
    private static final float CLEARED = 1.f;
    private static final float DRAWN = 0.5f;

    /** A vertex shader that gives the fragment shader what the real one does: the texture coordinates and the vertex alpha */
    private static final String VERTEX = """
            #version 330 core
            uniform float vertexAlpha;
            out vec2 vertexTextureCoordinates;
            out vec4 vertexColor;
            void main() {
                vec2 position = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                vertexTextureCoordinates = position;
                vertexColor = vec4(1.0, 1.0, 1.0, vertexAlpha);
                gl_Position = vec4(position * 2.0 - 1.0, 0.0, 1.0);
            }
            """;

    private static int program;

    private int framebuffer;
    private int depth;
    private int texture;
    private int vertexArray;

    @BeforeAll
    static void context() {
        GLTestContext.require();

        program = glCreateProgram();
        attach(program, GL_VERTEX_SHADER, "test vertex", VERTEX);
        final var fragment = ShaderResources.expand("shadow/cutout-fragment.glsl");
        attach(program, GL_FRAGMENT_SHADER, "shadow/cutout-fragment.glsl", fragment.code());
        glLinkProgram(program);
        assertEquals(GL_TRUE, glGetProgrami(program, GL_LINK_STATUS), glGetProgramInfoLog(program));
    }

    @BeforeEach
    void createTargets() {
        depth = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depth);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, WIDTH, 1, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));

        // One opaque texel and one hole
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        try (final var stack = MemoryStack.stackPush()) {
            final ByteBuffer pixels = stack.bytes((byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 0);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 2, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        vertexArray = glGenVertexArrays();
    }

    @AfterEach
    void deleteTargets() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindVertexArray(0);
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDeleteFramebuffers(framebuffer);
        glDeleteTextures(depth);
        glDeleteTextures(texture);
        glDeleteVertexArrays(vertexArray);
    }

    @Test
    void testHoleCastsNoShadow() {
        final var depths = draw(new Settings());

        assertEquals(DRAWN, depths[0], EPSILON, "Left, opaque");
        assertEquals(DRAWN, depths[1], EPSILON, "Left, opaque");
        assertEquals(CLEARED, depths[2], EPSILON, "Right, a hole");
        assertEquals(CLEARED, depths[3], EPSILON, "Right, a hole");
    }

    @Test
    void testAlphaOfTheMaterialColorIsCounted() {
        final var settings = new Settings();
        settings.colorAlpha = 0.4f;

        assertAll(depths(settings, CLEARED, CLEARED, CLEARED, CLEARED));
    }

    @Test
    void testAlphaOfTheVertexIsCounted() {
        final var settings = new Settings();
        settings.vertexAlpha = 0.4f;

        assertAll(depths(settings, CLEARED, CLEARED, CLEARED, CLEARED));
    }

    @Test
    void testAlphasAreMultiplied() {
        final var settings = new Settings();
        settings.vertexAlpha = 0.8f;
        settings.colorAlpha = 0.8f;

        // 0.64 is above the cutoff 0.5 on the opaque texel, 0.64 * 0 is below it on the hole
        assertAll(depths(settings, DRAWN, DRAWN, CLEARED, CLEARED));

        settings.cutoff = 0.7f;
        assertAll(depths(settings, CLEARED, CLEARED, CLEARED, CLEARED));
    }

    @Test
    void testCutoffZeroCutsNothing() {
        final var settings = new Settings();
        settings.cutoff = 0.f;

        // The alpha is never below 0
        assertAll(depths(settings, DRAWN, DRAWN, DRAWN, DRAWN));
    }

    @Test
    void testWithoutTextureOnlyTheColorsAreUsed() {
        final var settings = new Settings();
        settings.texture = false;

        assertAll(depths(settings, DRAWN, DRAWN, DRAWN, DRAWN));

        settings.colorAlpha = 0.2f;
        assertAll(depths(settings, CLEARED, CLEARED, CLEARED, CLEARED));
    }

    @Test
    void testTilingRepeatsTheTexture() {
        final var settings = new Settings();
        settings.tiling = 2.f;

        // The texture is twice as dense: opaque, hole, opaque, hole
        assertAll(depths(settings, DRAWN, CLEARED, DRAWN, CLEARED));
    }

    @Test
    void testOffsetShiftsTheTexture() {
        final var settings = new Settings();
        settings.offsetX = 0.5f;

        // The hole comes to the left
        assertAll(depths(settings, CLEARED, CLEARED, DRAWN, DRAWN));
    }

    private Executable[] depths(final Settings settings, final float... expected) {
        final var actual = draw(settings);
        final var checks = new Executable[expected.length];
        for (int i = 0; i < expected.length; i++) {
            final var index = i;
            checks[i] = () -> assertEquals(expected[index], actual[index], EPSILON, "The depth of the pixel " + index);
        }

        return checks;
    }

    /** Draws the triangle with the settings and returns the depth of every pixel */
    private float[] draw(final Settings settings) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(0, 0, WIDTH, 1);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glClearDepth(1.0);
        glClear(GL_DEPTH_BUFFER_BIT);
        glDisable(GL_BLEND);
        glDisable(GL_CULL_FACE);

        glUseProgram(program);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(glGetUniformLocation(program, "textureDiffuse"), 0);
        glUniform1i(glGetUniformLocation(program, "useTextureDiffuse"), settings.texture ? 1 : 0);
        glUniform1i(glGetUniformLocation(program, "useMaterial"), 1);
        glUniform4f(glGetUniformLocation(program, "material.color"), 1.f, 1.f, 1.f, settings.colorAlpha);
        glUniform2f(glGetUniformLocation(program, "material.tiling"), settings.tiling, 1.f);
        glUniform2f(glGetUniformLocation(program, "material.offset"), settings.offsetX, 0.f);
        glUniform1f(glGetUniformLocation(program, "material.alphaCutoff"), settings.cutoff);
        glUniform1f(glGetUniformLocation(program, "vertexAlpha"), settings.vertexAlpha);

        glBindVertexArray(vertexArray);
        glDrawArrays(GL_TRIANGLES, 0, 3);

        final var depths = new float[WIDTH];
        try (final var stack = MemoryStack.stackPush()) {
            final FloatBuffer buffer = stack.mallocFloat(WIDTH);
            glReadBuffer(GL_NONE);
            glBindTexture(GL_TEXTURE_2D, depth);
            glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, buffer);
            buffer.get(depths);
        }

        return depths;
    }

    private static void attach(final int program, final int type, final String name, final String code) {
        final var shader = glCreateShader(type);
        glShaderSource(shader, code);
        glCompileShader(shader);
        assertEquals(GL_TRUE, glGetShaderi(shader, GL_COMPILE_STATUS), name + ": " + glGetShaderInfoLog(shader));
        glAttachShader(program, shader);
    }

    /** The parameters of a draw: what the material and the vertices of a surface give the shader */
    private static final class Settings {
        private boolean texture = true;
        private float cutoff = 0.5f;
        private float colorAlpha = 1.f;
        private float vertexAlpha = 1.f;
        private float tiling = 1.f;
        private float offsetX = 0.f;
    }
}
