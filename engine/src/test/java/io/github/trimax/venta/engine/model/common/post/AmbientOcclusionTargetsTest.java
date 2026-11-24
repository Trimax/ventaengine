package io.github.trimax.venta.engine.model.common.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.glIsTexture;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.shaders.GLTestContext;

final class AmbientOcclusionTargetsTest {
    @Test
    void testSceneWithoutPixelsIsRefused() throws Exception {
        final var memory = TestMemory.create();

        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusionTargets(memory, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusionTargets(memory, 10, 0));
        assertThrows(NullPointerException.class, () -> new AmbientOcclusionTargets(null, 10, 10));
    }

    @Test
    void testTargetsAreHalfOfTheSceneAndReleased() throws Exception {
        GLTestContext.require();

        final var targets = new AmbientOcclusionTargets(TestMemory.create(), 101, 64);

        assertEquals(51, targets.getWidth());
        assertEquals(32, targets.getHeight());
        assertNotEquals(targets.getTextureID(0), targets.getTextureID(1), "The blur goes from one image to the other");
        assertNotEquals(targets.getFramebufferID(0), targets.getFramebufferID(1));
        assertTrue(glIsTexture(targets.getTextureID(0)) && glIsTexture(targets.getTextureID(1)));

        final var first = targets.getTextureID(0);
        final var second = targets.getTextureID(1);
        targets.release();

        assertFalse(glIsTexture(first) || glIsTexture(second), "The textures are deleted");
        assertDoesNotThrow(targets::release, "A second release is harmless");
    }
}
