package io.github.trimax.venta.engine.model.common.shadow;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.shaders.GLTestContext;
import io.github.trimax.venta.engine.shaders.ShaderResources;

/**
 * The shadow map on the GPU: the texture, drawing into its layers with the depth program of the shadow pass, the release.
 */
final class ShadowMapTest {
    private static final int RESOLUTION = 32;
    private static final int LAYERS = 3;
    private static final float EPSILON = 1e-5f;

    @BeforeAll
    static void context() {
        GLTestContext.require();
    }

    @Test
    void testTextureIsAnArrayOfDepthLayersThatCompares() throws Exception {
        final var map = new ShadowMap(TestMemory.create(), RESOLUTION, LAYERS);

        glBindTexture(GL_TEXTURE_2D_ARRAY, map.getTextureID());
        assertEquals(RESOLUTION, glGetTexLevelParameteri(GL_TEXTURE_2D_ARRAY, 0, GL_TEXTURE_WIDTH), "The width");
        assertEquals(RESOLUTION, glGetTexLevelParameteri(GL_TEXTURE_2D_ARRAY, 0, GL_TEXTURE_HEIGHT), "The height");
        assertEquals(LAYERS, glGetTexLevelParameteri(GL_TEXTURE_2D_ARRAY, 0, GL_TEXTURE_DEPTH), "A layer for every cascade");
        assertEquals(GL_COMPARE_REF_TO_TEXTURE, glGetTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_MODE), "A shadow sampler compares");
        assertEquals(GL_LEQUAL, glGetTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_FUNC), "A point is lit when it isn't behind the stored depth");
        assertEquals(GL_LINEAR, glGetTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER), "The hardware filters the comparisons");
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        map.release();
    }

    @Test
    void testLayersAreDrawnSeparately() throws Exception {
        final var map = new ShadowMap(TestMemory.create(), RESOLUTION, LAYERS);

        for (int layer = 0; layer < LAYERS; layer++) {
            map.bindLayer(layer);
            glClearDepth(0.25 * (layer + 1));
            glClear(GL_DEPTH_BUFFER_BIT);
        }

        for (int layer = 0; layer < LAYERS; layer++) {
            map.bindLayer(layer);
            assertEquals(0.25f * (layer + 1), depth(RESOLUTION / 2, RESOLUTION / 2), EPSILON, "The depth of the layer " + layer);
        }

        glClearDepth(1.0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        map.release();
    }

    @Test
    void testDepthProgramDrawsTheDepthOfAGeometry() throws Exception {
        final var map = new ShadowMap(TestMemory.create(), RESOLUTION, LAYERS);
        final var program = depthProgram();

        map.bindLayer(1);
        glClearDepth(1.0);
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glUseProgram(program);
        identity(program, "matrixViewProjection");
        identity(program, "matrixModel");

        // A triangle that covers the middle of the map at the clip depth 0.5, that is the depth 0.75
        final var vertexArray = glGenVertexArrays();
        final var buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, new float[]{-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.f, 0.5f, 0.5f}, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);

        assertEquals(0.75f, depth(RESOLUTION / 2, RESOLUTION / 2 - 4), EPSILON, "The triangle is in the middle of the map");
        assertEquals(1.f, depth(1, 1), EPSILON, "The corner of the map is empty");

        glBindVertexArray(0);
        glDeleteVertexArrays(vertexArray);
        glDeleteBuffers(buffer);
        glUseProgram(0);
        glDeleteProgram(program);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        map.release();
    }

    @Test
    void testDepthClampKeepsTheGeometryBeforeTheNearPlane() throws Exception {
        final var map = new ShadowMap(TestMemory.create(), RESOLUTION, LAYERS);
        final var program = depthProgram();

        map.bindLayer(0);
        glClearDepth(1.0);
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glUseProgram(program);
        identity(program, "matrixViewProjection");
        identity(program, "matrixModel");

        final var vertexArray = glGenVertexArrays();
        final var buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        // Nearer than the near plane: without the clamp it is clipped away and doesn't cast a shadow
        glBufferData(GL_ARRAY_BUFFER, new float[]{-0.5f, -0.5f, -2.f, 0.5f, -0.5f, -2.f, 0.f, 0.5f, -2.f}, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, 3);
        assertEquals(1.f, depth(RESOLUTION / 2, RESOLUTION / 2 - 4), EPSILON, "Clipped without the depth clamp");

        glEnable(GL_DEPTH_CLAMP);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glDisable(GL_DEPTH_CLAMP);
        assertEquals(0.f, depth(RESOLUTION / 2, RESOLUTION / 2 - 4), EPSILON, "Clamped to the near plane with it");

        glBindVertexArray(0);
        glDeleteVertexArrays(vertexArray);
        glDeleteBuffers(buffer);
        glUseProgram(0);
        glDeleteProgram(program);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        map.release();
    }

    @Test
    void testLayerIsChecked() throws Exception {
        final var map = new ShadowMap(TestMemory.create(), RESOLUTION, LAYERS);

        assertThrows(IllegalArgumentException.class, () -> map.bindLayer(-1), "No layer before the first");
        assertThrows(IllegalArgumentException.class, () -> map.bindLayer(LAYERS), "No layer after the last");
        assertThrows(IllegalArgumentException.class, () -> new ShadowMap(TestMemory.create(), 0, LAYERS), "No texels");
        assertThrows(IllegalArgumentException.class, () -> new ShadowMap(TestMemory.create(), RESOLUTION, 0), "No layers");

        map.release();
    }

    @Test
    void testReleaseDeletesTheTextureAndTheFramebuffer() throws Exception {
        final var map = new ShadowMap(TestMemory.create(), RESOLUTION, LAYERS);
        final var framebuffer = framebuffer(map);

        assertTrue(glIsTexture(map.getTextureID()), "The texture is created");
        assertTrue(glIsFramebuffer(framebuffer), "The framebuffer is created");

        map.release();

        assertFalse(glIsTexture(map.getTextureID()), "The texture must be deleted");
        assertFalse(glIsFramebuffer(framebuffer), "The framebuffer must be deleted");
    }

    /** The depth of the bound layer at a texel */
    private static float depth(final int x, final int y) {
        try (final var stack = MemoryStack.stackPush()) {
            final FloatBuffer pixel = stack.mallocFloat(1);
            glReadPixels(x, y, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, pixel);

            return pixel.get(0);
        }
    }

    private static int framebuffer(final ShadowMap map) throws Exception {
        final var field = ShadowMap.class.getDeclaredField("framebufferID");
        field.setAccessible(true);

        return field.getInt(map);
    }

    private static void identity(final int program, final String uniform) {
        glUniformMatrix4fv(glGetUniformLocation(program, uniform), false, new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
    }

    /** The program of the shadow pass for objects, compiled from the resources of the engine */
    private static int depthProgram() {
        final var program = glCreateProgram();
        for (final var shader : new String[]{"shadow/vertex.glsl", "shadow/fragment.glsl"}) {
            final var source = ShaderResources.expand(shader);
            final var id = glCreateShader(shader.endsWith("vertex.glsl") ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER);
            glShaderSource(id, source.code());
            glCompileShader(id);
            assertEquals(GL_TRUE, glGetShaderi(id, GL_COMPILE_STATUS), shader + ": " + source.mapLog(glGetShaderInfoLog(id)));
            glAttachShader(program, id);
        }

        glLinkProgram(program);
        assertEquals(GL_TRUE, glGetProgrami(program, GL_LINK_STATUS), glGetProgramInfoLog(program));
        return program;
    }
}
