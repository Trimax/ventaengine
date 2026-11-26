package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.studio.model.property.Property;

final class PropertyUtilTest {
    public record Inner(float density, Boolean enabled) {
    }

    public record Item(String name, int size) {
    }

    public record Thing(String name, Vector3f position, Inner inner, Integer missing, List<Item> items, List<String> tags, Mode mode) {
    }

    public enum Mode {
        Fast
    }

    private static Property find(final List<Property> properties, final String name) {
        return properties.stream().filter(property -> property.name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void testEveryFieldOfARecordIsALine() {
        final var lines = PropertyUtil.read(new Thing("Chest", new Vector3f(2.f, 1.f, 23.5f), new Inner(0.25f, true), null, List.of(), List.of(), Mode.Fast));

        assertEquals("Chest", find(lines, "Name").value());
        assertEquals("2.0, 1.0, 23.5", find(lines, "Position").value(), "Vectors are in plain numbers");
        assertEquals("Fast", find(lines, "Mode").value());
    }

    @Test
    void testFieldWithoutAValueIsNotShown() {
        final var lines = PropertyUtil.read(new Thing("Chest", new Vector3f(), null, null, null, null, null));

        assertTrue(lines.stream().noneMatch(line -> line.name().equals("Missing")));
        assertTrue(lines.stream().noneMatch(line -> line.name().equals("Inner")));
        assertEquals(2, lines.size());
    }

    @Test
    void testRecordInsideARecordHasItsFieldsAsParts() {
        final var inner = find(PropertyUtil.read(new Thing("a", new Vector3f(), new Inner(0.5f, false), null, List.of(), List.of(), Mode.Fast)), "Inner");

        assertEquals("", inner.value());
        assertEquals("0.5", find(inner.children(), "Density").value());
        assertEquals("false", find(inner.children(), "Enabled").value());
    }

    @Test
    void testListHasItsElementsByTheirNames() {
        final var items = find(PropertyUtil.read(new Thing("a", new Vector3f(), null, null, List.of(new Item("First", 3), new Item("Second", 4)), List.of("x", "y"), null)), "Items");

        assertEquals("2 items", items.value());
        assertEquals(List.of("First", "Second"), items.children().stream().map(Property::name).toList());
        assertEquals("3", find(items.children().get(0).children(), "Size").value());
    }

    @Test
    void testListOfValuesHasItsElementsByTheirPlaces() {
        final var tags = find(PropertyUtil.read(new Thing("a", new Vector3f(), null, null, null, List.of("x"), null)), "Tags");

        assertEquals("1 item", tags.value());
        assertEquals("#0", tags.children().getFirst().name());
        assertEquals("x", tags.children().getFirst().value());
    }

    @Test
    void testNothingHasNoLines() {
        assertTrue(PropertyUtil.read(null).isEmpty());
    }

    @Test
    void testValueThatIsNotARecordIsOneLine() {
        final var lines = PropertyUtil.read(new Vector3f(1.f, 2.f, 3.f));

        assertEquals(1, lines.size());
        assertEquals("1.0, 2.0, 3.0", lines.getFirst().value());
    }

    @Test
    void testNamesAreWords() {
        assertEquals("Cast shadows", PropertyUtil.humanize("castShadows"));
        assertEquals("Exposure", PropertyUtil.humanize("exposure"));
        assertEquals("", PropertyUtil.humanize(""));
    }

    @Test
    void testNumbersAreShortWithoutLosingTheirFraction() {
        final var lines = PropertyUtil.read(new Inner(0.123456f, null));

        assertEquals("0.1235", find(lines, "Density").value());
        assertEquals("10.0", find(PropertyUtil.read(new Inner(10.f, null)), "Density").value());
    }
}
