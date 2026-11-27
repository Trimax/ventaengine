package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

final class EntryUtilTest {
    private static JsonObject entry(final String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void testNameIsReadAndWritten() {
        final var entry = entry("{\"name\": \"Chest\"}");
        assertEquals("Chest", EntryUtil.getName(entry));

        EntryUtil.setName(entry, "Box");
        assertEquals("Box", EntryUtil.getName(entry));
        assertEquals("", EntryUtil.getName(entry("{}")));
    }

    @Test
    void testVectorIsReadFromTheFile() {
        final var entry = entry("{\"position\": {\"x\": 1, \"y\": 2.5, \"z\": -3}}");

        assertEquals(new Vector3f(1.f, 2.5f, -3.f), EntryUtil.getVector(entry, EntryUtil.POSITION, new Vector3f()));
    }

    @Test
    void testFieldThatTheFileDoesNotHaveIsTheFallback() {
        final var entry = entry("{}");

        assertEquals(new Vector3f(1.f), EntryUtil.getVector(entry, EntryUtil.SCALE, new Vector3f(1.f)));
        assertEquals(new Vector2f(2.f, 3.f), EntryUtil.getVector(entry, EntryUtil.SIZE, new Vector2f(2.f, 3.f)));
        assertFalse(EntryUtil.has(entry, EntryUtil.SCALE));
    }

    @Test
    void testWrittenVectorIsAnObjectOfTheAxes() {
        final var entry = entry("{}");

        EntryUtil.setVector(entry, EntryUtil.POSITION, new Vector3f(1.f, 2.f, 3.f));
        EntryUtil.setVector(entry, EntryUtil.SIZE, new Vector2f(4.f, 5.f));

        assertTrue(EntryUtil.has(entry, EntryUtil.POSITION));
        assertEquals("{\"x\":1.0,\"y\":2.0,\"z\":3.0}", entry.get("position").toString());
        assertEquals("{\"x\":4.0,\"y\":5.0}", entry.get("size").toString());
    }

    @Test
    void testNumbersAreWrittenWithFourDecimalsAtMost() {
        final var entry = entry("{}");

        EntryUtil.setVector(entry, EntryUtil.POSITION, new Vector3f(0.1f + 0.2f, 1.f / 3.f, 123.456789f));

        assertEquals("{\"x\":0.3,\"y\":0.3333,\"z\":123.4568}", entry.get("position").toString());
    }

    @Test
    void testPartOfAVectorIsTheFallbackForTheRest() {
        final var entry = entry("{\"position\": {\"x\": 7}}");

        assertEquals(new Vector3f(7.f, 5.f, 5.f), EntryUtil.getVector(entry, EntryUtil.POSITION, new Vector3f(5.f)));
    }

    @Test
    void testWrittenVectorReplacesTheOldOne() {
        final var entry = entry("{\"position\": {\"x\": 1, \"y\": 1, \"z\": 1}, \"name\": \"A\"}");

        EntryUtil.setVector(entry, EntryUtil.POSITION, new Vector3f(9.f, 8.f, 7.f));

        assertEquals(new Vector3f(9.f, 8.f, 7.f), EntryUtil.getVector(entry, EntryUtil.POSITION, new Vector3f()));
        assertEquals("[position, name]", entry.keySet().toString(), "The field keeps its place");
    }
}
