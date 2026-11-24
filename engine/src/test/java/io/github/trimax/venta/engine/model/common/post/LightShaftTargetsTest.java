package io.github.trimax.venta.engine.model.common.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.glIsTexture;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.shaders.GLTestContext;

final class LightShaftTargetsTest {
    @Test
    void testImageIsHalfOfTheSceneRoundedUp() {
        assertEquals(800, LightShaftTargets.getSize(1600));
        assertEquals(601, LightShaftTargets.getSize(1201), "An odd size has a texel for the last pixel");
        assertEquals(1, LightShaftTargets.getSize(1));
        assertEquals(1, LightShaftTargets.getSize(2));
    }

    @Test
    void testSceneWithoutPixelsIsRefused() throws Exception {
        final var memory = TestMemory.create();

        assertThrows(IllegalArgumentException.class, () -> new LightShaftTargets(memory, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new LightShaftTargets(memory, 10, 0));
        assertThrows(NullPointerException.class, () -> new LightShaftTargets(null, 10, 10));
    }

    @Test
    void testTargetsAreMadeAndReleased() throws Exception {
        GLTestContext.require();

        final var targets = new LightShaftTargets(TestMemory.create(), 101, 64);

        assertEquals(51, targets.getWidth());
        assertEquals(32, targets.getHeight());
        assertEquals(101, targets.getSceneWidth());
        assertEquals(64, targets.getSceneHeight());
        assertTrue(glIsTexture(targets.getTextureID()));

        final var texture = targets.getTextureID();
        targets.release();

        assertFalse(glIsTexture(texture), "The texture is deleted");
        assertEquals(0, targets.getTextureID());
        assertDoesNotThrow(targets::release, "A second release is harmless");
    }
}
