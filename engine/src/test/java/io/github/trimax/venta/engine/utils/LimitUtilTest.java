package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class LimitUtilTest {
    @Test
    void testCountWithinLimits() {
        assertDoesNotThrow(() -> LimitUtil.checkCount("water.json", "waves", 0, 0, 16), "Minimal count");
        assertDoesNotThrow(() -> LimitUtil.checkCount("water.json", "waves", 16, 0, 16), "Maximal count");
    }

    @Test
    void testCountOutsideLimitsIsReported() {
        final var tooMany = assertThrows(IllegalArgumentException.class, () -> LimitUtil.checkCount("water.json", "waves", 17, 0, 16));
        assertTrue(tooMany.getMessage().contains("water.json"), "Message must name the resource");
        assertTrue(tooMany.getMessage().contains("17 waves"), "Message must name the items and their count");

        assertThrows(IllegalArgumentException.class, () -> LimitUtil.checkCount("terrain.json", "materials", 0, 1, 8), "Terrain needs a material");
    }

    @Test
    void testFit() {
        assertEquals(3, LimitUtil.fit(3, 16), "Count within the array");
        assertEquals(16, LimitUtil.fit(20, 16), "Count is cut to the array size");
        assertEquals(0, LimitUtil.fit(-1, 16), "Negative count");
    }
}
