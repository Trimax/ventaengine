package io.github.trimax.venta.engine.renderers.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.glIsTexture;
import static org.lwjgl.opengl.GL30C.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.shaders.GLTestContext;

/**
 * The GL objects of the post processing renderer are deleted by it: the memory of the engine reports and deletes
 * everything that is still there when the engine shuts down.
 */
final class PostProcessingRendererReleaseTest {
    private static final int SAMPLES = 4;

    @BeforeAll
    static void context() {
        GLTestContext.require();
    }

    @Test
    void testReleaseBeforeTheFirstFrameChangesNothing() throws Exception {
        final var renderer = renderer(TestMemory.create());

        assertDoesNotThrow(renderer::release, "There is nothing to release before the first frame");
    }

    @Test
    void testReleaseDeletesEveryObjectOfTheRenderer() throws Exception {
        final var memory = TestMemory.create();
        final var renderer = renderer(memory);

        renderer.begin(new WindowState(null, GLTestContext.window(), "test", 64, 64, SAMPLES));
        final var vertexArray = vertexArray(renderer);
        glBindVertexArray(0);

        final var framebuffers = new int[]{field(renderer, "sceneFramebufferID"), field(renderer, "resolvedFramebufferID")};
        final var renderbuffers = new int[]{field(renderer, "sceneColorID"), field(renderer, "sceneDepthID")};
        final var texture = field(renderer, "resolvedTextureID");
        for (final var framebuffer : framebuffers)
            assertTrue(glIsFramebuffer(framebuffer), "The framebuffer " + framebuffer + " is created by the first frame");
        for (final var renderbuffer : renderbuffers)
            assertTrue(glIsRenderbuffer(renderbuffer), "The renderbuffer " + renderbuffer + " is created by the first frame");
        assertTrue(glIsTexture(texture), "The resolved scene is created by the first frame");
        assertTrue(glIsVertexArray(vertexArray), "The vertex array is created for the post pass");

        renderer.release();

        for (final var framebuffer : framebuffers)
            assertFalse(glIsFramebuffer(framebuffer), "The framebuffer " + framebuffer + " must be deleted");
        for (final var renderbuffer : renderbuffers)
            assertFalse(glIsRenderbuffer(renderbuffer), "The renderbuffer " + renderbuffer + " must be deleted");
        assertFalse(glIsTexture(texture), "The resolved scene must be deleted");
        assertFalse(glIsVertexArray(vertexArray), "The vertex array must be deleted");
    }

    @Test
    void testReleasedRendererWorksAgainAndCanBeReleasedTwice() throws Exception {
        final var renderer = renderer(TestMemory.create());
        final var window = new WindowState(null, GLTestContext.window(), "test", 64, 64, SAMPLES);

        renderer.begin(window);
        renderer.release();
        assertDoesNotThrow(renderer::release, "A second release has nothing to delete");

        renderer.begin(window);
        assertTrue(glIsFramebuffer(field(renderer, "sceneFramebufferID")), "The next frame creates the framebuffers again");
        renderer.release();
    }

    /** The renderer is a component with a private constructor, the program registry isn't used before the post pass */
    private static PostProcessingRenderer renderer(final Memory memory) throws Exception {
        final Constructor<?> constructor = PostProcessingRenderer.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        return (PostProcessingRenderer) constructor.newInstance(null, memory);
    }

    private static int field(final PostProcessingRenderer renderer, final String name) throws Exception {
        final var field = PostProcessingRenderer.class.getDeclaredField(name);
        field.setAccessible(true);

        return field.getInt(renderer);
    }

    /** The vertex array of the post pass is created by the first post pass, here it is created and bound like there */
    private static int vertexArray(final PostProcessingRenderer renderer) throws Exception {
        final Method method = PostProcessingRenderer.class.getDeclaredMethod("getVertexArray");
        method.setAccessible(true);
        final var vertexArray = (int) method.invoke(renderer);
        glBindVertexArray(vertexArray);

        return vertexArray;
    }
}
