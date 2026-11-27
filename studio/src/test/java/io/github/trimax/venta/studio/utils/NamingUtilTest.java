package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

final class NamingUtilTest {
    @Test
    void testFreeNameIsKept() {
        assertEquals("Chest", NamingUtil.unique(List.of("Palm", "Ball"), "Chest"));
        assertEquals("Chest", NamingUtil.unique(List.of(), "Chest"));
    }

    @Test
    void testTakenNameGetsTheNextFreeNumber() {
        assertEquals("Chest 2", NamingUtil.unique(List.of("Chest"), "Chest"));
        assertEquals("Chest 3", NamingUtil.unique(List.of("Chest", "Chest 2"), "Chest"));
        assertEquals("Chest 4", NamingUtil.unique(List.of("Chest", "Chest 2", "Chest 3"), "Chest"));
    }

    @Test
    void testNumberOfTheNameIsTheStartOfTheNext() {
        assertEquals("Chest 3", NamingUtil.unique(List.of("Chest", "Chest 2"), "Chest 2"));
        assertEquals("Chest 6", NamingUtil.unique(List.of("Chest 5"), "Chest 5"));
        assertEquals("Chest 4", NamingUtil.unique(List.of("Chest 2", "Chest 3"), "Chest 2"), "A number in use is skipped");
    }

    @Test
    void testNameThatEndsWithADigitWithoutASpaceIsNotANumber() {
        assertEquals("Tree1 2", NamingUtil.unique(List.of("Tree1"), "Tree1"));
    }

    @Test
    void testNameOfAFileIsAReadableWord() {
        assertEquals("Pine", NamingUtil.nameOf("trees/pine.json"));
        assertEquals("Leafy 3", NamingUtil.nameOf("trees/leafy-3.json"));
        assertEquals("Chest beach", NamingUtil.nameOf("chest-beach.json"));
        assertEquals("Surf left", NamingUtil.nameOf("surf_left.json"));
        assertEquals("Thing", NamingUtil.nameOf(".json"));
    }
}
