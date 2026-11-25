package io.github.trimax.venta.engine.model.common.reflection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class NearestSelectionTest {
    @Test
    void testKeepsTheNearestInTheOrderOfTheDistance() {
        final var selection = new NearestSelection<String>(3);
        selection.offer("far", 50.f);
        selection.offer("near", 1.f);
        selection.offer("middle", 10.f);
        selection.offer("farther", 90.f);
        selection.offer("nearer", 0.5f);

        assertEquals(3, selection.size());
        assertEquals("nearer", selection.get(0));
        assertEquals("near", selection.get(1));
        assertEquals("middle", selection.get(2));
        assertFalse(selection.contains("far"));
    }

    @Test
    void testEqualDistanceDoesNotReplaceWhatIsKept() {
        final var selection = new NearestSelection<String>(1);
        selection.offer("first", 4.f);
        selection.offer("second", 4.f);

        assertEquals("first", selection.get(0));
    }

    @Test
    void testFewerThingsThanRoomAreAllKept() {
        final var selection = new NearestSelection<String>(4);
        selection.offer("a", 2.f);
        selection.offer("b", 1.f);

        assertEquals(2, selection.size());
        assertTrue(selection.asPredicate().test("a"));
        assertThrows(IndexOutOfBoundsException.class, () -> selection.get(2));
    }

    @Test
    void testContainsIsByIdentity() {
        final var selection = new NearestSelection<String>(2);
        final var kept = new String("same");
        selection.offer(kept, 1.f);

        assertTrue(selection.contains(kept));
        assertFalse(selection.contains(new String("same")));
    }

    @Test
    void testClearForgetsEverything() {
        final var selection = new NearestSelection<String>(2);
        selection.offer("a", 1.f);
        selection.clear();

        assertEquals(0, selection.size());
        assertFalse(selection.contains("a"));

        selection.offer("b", 5.f);
        assertEquals("b", selection.get(0));
    }

    @Test
    void testCapacityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new NearestSelection<String>(0));
    }
}
