package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

final class RenderOrderUtilTest {
    private record Item(String name, boolean solid, double distance) {
    }

    private static List<String> order(final List<Item> items) {
        return RenderOrderUtil.order(items, Item::solid, Item::distance).stream().map(Item::name).toList();
    }

    @Test
    void testSolidObjectsGoFirstInTheirOrder() {
        final var items = List.of(new Item("far glass", false, 10), new Item("wall", true, 2), new Item("leaves", true, 30), new Item("near glass", false, 1));

        assertEquals(List.of("wall", "leaves", "far glass", "near glass"), order(items));
    }

    @Test
    void testObjectsThatMixColorsGoFromTheFarthestToTheNearest() {
        final var items = List.of(new Item("b", false, 5), new Item("c", false, 9), new Item("a", false, 1));

        assertEquals(List.of("c", "b", "a"), order(items));
    }

    @Test
    void testObjectsAtTheSameDistanceKeepTheirOrder() {
        final var items = List.of(new Item("first", false, 4), new Item("second", false, 4), new Item("third", false, 4));

        assertEquals(List.of("first", "second", "third"), order(items));
    }

    @Test
    void testEmptyAndSingleCollections() {
        assertEquals(List.of(), order(List.of()));
        assertEquals(List.of("only"), order(List.of(new Item("only", false, 3))));
    }

    @Test
    void testSourceIsNotChanged() {
        final var items = List.of(new Item("near", false, 1), new Item("far", false, 9));

        order(items);

        assertEquals(List.of("near", "far"), items.stream().map(Item::name).toList(), "The scene keeps its own order");
    }

    @Test
    void testAnyCollectionIsAccepted() {
        final var items = Set.of(new Item("only", true, 0));

        assertEquals(1, RenderOrderUtil.order(items, Item::solid, Item::distance).size());
    }

    @Test
    void testNullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> RenderOrderUtil.order(null, Item::solid, Item::distance));
        assertThrows(NullPointerException.class, () -> RenderOrderUtil.order(List.<Item>of(), null, Item::distance));
        assertThrows(NullPointerException.class, () -> RenderOrderUtil.order(List.<Item>of(), Item::solid, null));
    }
}
