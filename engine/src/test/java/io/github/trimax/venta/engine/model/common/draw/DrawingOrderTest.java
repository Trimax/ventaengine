package io.github.trimax.venta.engine.model.common.draw;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

final class DrawingOrderTest {
    private record Item(String name, boolean solid, double distance) {
    }

    private static List<String> order(final List<Item> items) {
        return new DrawingOrder<Item>().order(items, Item::solid, Item::distance).stream().map(Item::name).toList();
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

        assertEquals(1, new DrawingOrder<Item>().order(items, Item::solid, Item::distance).size());
    }

    @Test
    void testNullArgumentsAreRejected() {
        final var order = new DrawingOrder<Item>();

        assertThrows(NullPointerException.class, () -> order.order(null, Item::solid, Item::distance));
        assertThrows(NullPointerException.class, () -> order.order(List.of(), null, Item::distance));
        assertThrows(NullPointerException.class, () -> order.order(List.of(), Item::solid, null));
    }

    @Test
    void testDistanceIsAskedOncePerObject() {
        final var asked = new int[1];
        final var items = new ArrayList<Item>();
        for (int i = 0; i < 100; i++)
            items.add(new Item("item" + i, false, (i * 37) % 11));

        new DrawingOrder<Item>().order(items, Item::solid, item -> {
            asked[0]++;
            return item.distance();
        });

        assertEquals(100, asked[0], "A distance can be costly to compute, a sort asks for it many times");
    }

    @Test
    void testManyObjectsAreOrderedLikeAStableSort() {
        final var random = new Random(42);
        final var items = new ArrayList<Item>();
        // Distances repeat, so the stability of the sort is checked as well; the count is above the initial capacity
        for (int i = 0; i < 1000; i++)
            items.add(new Item("item" + i, random.nextInt(5) == 0, random.nextInt(50)));

        final var expected = new ArrayList<Item>();
        items.stream().filter(Item::solid).forEach(expected::add);
        expected.addAll(items.stream().filter(item -> !item.solid()).sorted(Comparator.comparingDouble(Item::distance).reversed()).toList());

        assertEquals(expected, new DrawingOrder<Item>().order(items, Item::solid, Item::distance));
    }

    @Test
    void testSizesAroundThePowersOfTwo() {
        for (final var size : new int[]{2, 3, 4, 5, 7, 8, 9, 15, 16, 17, 31, 33, 64, 65, 129}) {
            final var random = new Random(size);
            final var items = new ArrayList<Item>();
            for (int i = 0; i < size; i++)
                items.add(new Item("item" + i, false, random.nextInt(20)));

            final var expected = items.stream().sorted(Comparator.comparingDouble(Item::distance).reversed()).toList();

            assertEquals(expected, new DrawingOrder<Item>().order(items, Item::solid, Item::distance), "The size " + size);
        }
    }

    @Test
    void testOneInstanceIsReusedAndGivesTheRightResultEveryTime() {
        final var order = new DrawingOrder<Item>();
        final var first = order.order(List.of(new Item("a", false, 1), new Item("b", false, 2)), Item::solid, Item::distance);
        assertEquals(List.of("b", "a"), first.stream().map(Item::name).toList());

        final var second = order.order(List.of(new Item("c", true, 0), new Item("d", false, 5), new Item("e", false, 6)), Item::solid, Item::distance);

        assertSame(first, second, "The list is reused: no allocation per frame");
        assertEquals(List.of("c", "e", "d"), second.stream().map(Item::name).toList(), "Nothing is left from the previous call");

        assertEquals(List.of(), order.order(List.of(), Item::solid, Item::distance), "An empty scene after a full one");
    }
}
