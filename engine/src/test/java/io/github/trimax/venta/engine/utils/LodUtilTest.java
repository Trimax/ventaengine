package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class LodUtilTest {
    private static final float EPSILON = 1e-6f;

    /** The levels 1 and 2 are used below 30% and below 10% of the height of the screen */
    private static final float[] SIZES = {0.3f, 0.1f};

    @Test
    void testSphereTakesAShareOfTheScreenThatFallsWithTheDistance() {
        assertEquals(0.5f, LodUtil.screenSize(1.f, 2.f, 1.f), EPSILON, "A sphere of the radius 1 at the distance 2 and 90 degrees fills half of the screen");
        assertEquals(0.25f, LodUtil.screenSize(1.f, 4.f, 1.f), EPSILON);
        assertEquals(0.25f, LodUtil.screenSize(2.f, 8.f, 1.f), EPSILON, "A bigger object that is farther by the same ratio looks the same");
    }

    @Test
    void testNarrowerViewMakesObjectsBigger() {
        // The focal length is 1 / tan(fov / 2): a narrower field of view has a longer one
        assertEquals(0.5f * 2.f, LodUtil.screenSize(1.f, 2.f, 2.f), EPSILON);
    }

    @Test
    void testCameraInsideOfTheObjectDoesNotDivideByZero() {
        assertTrue(Float.isFinite(LodUtil.screenSize(1.f, 0.f, 1.f)));
        assertTrue(LodUtil.screenSize(1.f, 0.f, 1.f) > 1.f, "Inside of an object it is as big as it can be");
    }

    @Test
    void testTheLevelFollowsTheSize() {
        assertEquals(0, LodUtil.select(SIZES, 0, 0.9f, 0.f));
        assertEquals(1, LodUtil.select(SIZES, 0, 0.2f, 0.f));
        assertEquals(2, LodUtil.select(SIZES, 0, 0.05f, 0.f));
        assertEquals(0, LodUtil.select(SIZES, 2, 0.9f, 0.f), "It can go back through every level");
        assertEquals(1, LodUtil.select(SIZES, 2, 0.2f, 0.f));
    }

    @Test
    void testObjectWithoutSimplerModelsHasTheLevelZero() {
        assertEquals(0, LodUtil.select(new float[0], 0, 0.001f, 0.1f));
    }

    @Test
    void testBorderIsMovedByTheHysteresisTowardsTheLevelThatIsDrawn() {
        // The border of the level 1 is 0.3: from the level 0 the size must be below 0.27, from the level 1 above 0.33
        assertEquals(0, LodUtil.select(SIZES, 0, 0.29f, 0.1f), "Just below the border it stays");
        assertEquals(1, LodUtil.select(SIZES, 0, 0.26f, 0.1f));
        assertEquals(1, LodUtil.select(SIZES, 1, 0.31f, 0.1f), "Just above the border it stays");
        assertEquals(0, LodUtil.select(SIZES, 1, 0.34f, 0.1f));
    }

    @Test
    void testSizeThatFloatsAroundABorderDoesNotChangeTheLevelEveryFrame() {
        var level = 0;
        var changes = 0;
        for (int frame = 0; frame < 200; frame++) {
            final var size = 0.3f + (frame % 2 == 0 ? 0.01f : -0.01f);
            final var next = LodUtil.select(SIZES, level, size, 0.1f);
            if (next != level)
                changes++;

            level = next;
        }

        assertEquals(0, changes, "The size stays in the hysteresis band");
    }

    @Test
    void testLevelsAreSkippedWhenTheSizeFallsFast() {
        assertEquals(2, LodUtil.select(SIZES, 0, 0.01f, 0.1f));
    }

    @Test
    void testBadInputsKeepTheLevel() {
        assertEquals(1, LodUtil.select(SIZES, 1, Float.NaN, 0.1f));
        assertEquals(2, LodUtil.select(SIZES, 7, 0.2f, 0.f) + 1, "A level beyond the last one is brought back");
        assertEquals(0, LodUtil.select(SIZES, -3, 0.9f, 0.f));
        assertThrows(NullPointerException.class, () -> LodUtil.select(null, 0, 1.f, 0.f));
    }
}
