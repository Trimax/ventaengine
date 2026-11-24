package io.github.trimax.venta.engine.model.common.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.shaders.GLTestContext;

final class BloomTargetsTest {
    @Test
    void testLevelsAreAsManyAsKeepTheSmallestLevelAboveAFewTexels() {
        assertEquals(6, BloomTargets.getLevelCount(1600, 1200), "A big image gets the most levels");
        assertEquals(4, BloomTargets.getLevelCount(64, 64), "32, 16, 8 and 4");
        assertEquals(5, BloomTargets.getLevelCount(1024, 128), "The smaller side decides: 64, 32, 16, 8 and 4");
        assertEquals(1, BloomTargets.getLevelCount(8, 8));
    }

    @Test
    void testThereIsAlwaysALevel() {
        assertEquals(1, BloomTargets.getLevelCount(1, 1));
        assertEquals(1, BloomTargets.getLevelCount(4, 4000));
    }

    @Test
    void testLevelsAreHalvedFromHalfOfTheImage() {
        assertEquals(800, BloomTargets.getLevelSize(1600, 0));
        assertEquals(400, BloomTargets.getLevelSize(1600, 1));
        assertEquals(25, BloomTargets.getLevelSize(1600, 5));
        assertEquals(1, BloomTargets.getLevelSize(1, 0), "A level is never empty");
        assertEquals(1, BloomTargets.getLevelSize(3, 5));
    }

    @Test
    void testChainIsNotLongerThanTheLimit() {
        assertTrue(BloomTargets.getLevelCount(100000, 100000) <= BloomTargets.MAXIMAL_LEVELS);
    }

    @Test
    void testImageWithoutPixelsIsRefused() throws Exception {
        final var memory = TestMemory.create();

        assertThrows(IllegalArgumentException.class, () -> new BloomTargets(memory, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new BloomTargets(memory, 10, -1));
        assertThrows(NullPointerException.class, () -> new BloomTargets(null, 10, 10));
    }

    /** The targets of a chain on the GPU */
    @Test
    void testTargetsAreMadeForEveryLevelAndReleased() throws Exception {
        GLTestContext.require();

        final var memory = TestMemory.create();
        final var targets = new BloomTargets(memory, 64, 48);

        assertEquals(3, targets.getLevels(), "24, 12 and 6");
        for (int level = 0; level < targets.getLevels(); level++) {
            assertEquals(BloomTargets.getLevelSize(64, level), targets.getWidth(level));
            assertEquals(BloomTargets.getLevelSize(48, level), targets.getHeight(level));
            assertTrue(glIsTexture(targets.getTextureID(level)), "The texture of the level " + level);
        }

        final var first = targets.getTextureID(0);
        targets.release();

        assertFalse(glIsTexture(first), "The texture is deleted");
    }
}
