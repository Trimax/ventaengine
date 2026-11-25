package io.github.trimax.venta.engine.model.instance.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

final class LightRangeTest {
    @Test
    void testLightTakesTheRangeOfItsPrefab() {
        final var light = TestObjects.light("light", new Vector3f(), 25.f, false);

        assertEquals(25.f, light.getRange());
    }

    @Test
    void testRangeCanBeChanged() {
        final var light = TestObjects.light("light", new Vector3f(), 25.f, false);

        light.setRange(0.f);
        assertEquals(0.f, light.getRange(), "A light without a range reaches as far as its attenuation does");

        light.setRange(80.f);
        assertEquals(80.f, light.getRange());
    }

    @Test
    void testBadRangeIsRefusedAndChangesNothing() {
        final var light = TestObjects.light("light", new Vector3f(), 25.f, false);

        assertThrows(IllegalArgumentException.class, () -> light.setRange(-1.f));
        assertThrows(IllegalArgumentException.class, () -> light.setRange(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> light.setRange(Float.POSITIVE_INFINITY));

        assertEquals(25.f, light.getRange());
    }
}
