package io.github.trimax.venta.engine.model.common.shadow;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.instance.implementation.TestObjects;

final class PointShadowFrameTest {
    private final PointShadowFrame frame = new PointShadowFrame(3, 6);

    @Test
    void testNewFrameIsEmptyAndNotActive() {
        assertEquals(0, frame.getCount());
        assertFalse(frame.isActive());
        assertEquals(3, frame.getCapacity());
        assertEquals(6, frame.getFaces());
    }

    @Test
    void testLightsGetTheNextSlots() {
        final var first = TestObjects.light("first", new Vector3f(), 10.f, true);
        final var second = TestObjects.light("second", new Vector3f(), 10.f, true);

        assertEquals(0, frame.add(first, 10.f));
        assertEquals(1, frame.add(second, 20.f));

        assertTrue(frame.isActive());
        assertEquals(2, frame.getCount());
        assertSame(first, frame.getLight(0));
        assertSame(second, frame.getLight(1));
        assertEquals(20.f, frame.getRange(1));
    }

    @Test
    void testSlotIsFoundByTheLightItself() {
        final var first = TestObjects.light("same name", new Vector3f(), 10.f, true);
        final var second = TestObjects.light("same name", new Vector3f(), 10.f, true);
        final var other = TestObjects.light("other", new Vector3f(), 10.f, true);
        frame.add(first, 10.f);
        frame.add(second, 10.f);

        assertEquals(0, frame.getSlot(first));
        assertEquals(1, frame.getSlot(second), "Lights are told apart by reference");
        assertEquals(-1, frame.getSlot(other), "A light without a map has no slot");
    }

    @Test
    void testFrameWithoutRoomRefusesAnotherLight() {
        for (int i = 0; i < 3; i++)
            frame.add(TestObjects.light("light " + i, new Vector3f(), 10.f, true), 10.f);

        assertThrows(IllegalStateException.class, () -> frame.add(TestObjects.light("one more", new Vector3f(), 10.f, true), 10.f));
    }

    @Test
    void testClearForgetsTheLights() {
        final var light = TestObjects.light("light", new Vector3f(), 10.f, true);
        frame.add(light, 10.f);

        frame.clear();

        assertFalse(frame.isActive());
        assertEquals(-1, frame.getSlot(light));
        assertNull(frame.getLight(0), "The frame doesn't keep a light alive");
    }

    @Test
    void testFacesOfSlotsAreSeparateMatrices() {
        assertNotSame(frame.getMatrix(0, 0), frame.getMatrix(0, 1));
        assertNotSame(frame.getMatrix(0, 5), frame.getMatrix(1, 0));
        assertSame(frame.getMatrix(2, 3), frame.getMatrix(2, 3));
    }

    @Test
    void testBadSizesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new PointShadowFrame(0, 6));
        assertThrows(IllegalArgumentException.class, () -> new PointShadowFrame(4, 0));
    }
}
