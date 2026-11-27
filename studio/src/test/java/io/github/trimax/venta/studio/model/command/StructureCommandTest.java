package io.github.trimax.venta.studio.model.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import io.github.trimax.venta.studio.model.outline.OutlineKind;

final class StructureCommandTest {
    private final List<String> calls = new ArrayList<>();
    private final StructureCommand.Store store = new StructureCommand.Store() {
        @Override
        public void insert(final OutlineKind kind, final int index, final JsonObject entry) {
            calls.add("insert " + kind + "#" + index + ":" + entry.get("v").getAsInt());
        }

        @Override
        public void remove(final OutlineKind kind, final int index) {
            calls.add("remove " + kind + "#" + index);
        }
    };

    private static JsonObject entry(final int value) {
        final var entry = new JsonObject();
        entry.addProperty("v", value);
        return entry;
    }

    @Test
    void testInsertionPutsTheThingAndUndoTakesItOut() {
        final var command = StructureCommand.insertion(store, OutlineKind.Object, 3, entry(7), "Create A");

        command.execute();
        command.revert();

        assertEquals(List.of("insert Object#3:7", "remove Object#3"), calls);
        assertEquals("Create A", command.label());
    }

    @Test
    void testRemovalTakesTheThingOutAndUndoPutsItBackInItsPlace() {
        final var command = StructureCommand.removal(store, OutlineKind.Light, 0, entry(9), "Delete B");

        command.execute();
        command.revert();

        assertEquals(List.of("remove Light#0", "insert Light#0:9"), calls);
    }

    @Test
    void testCommandKeepsACopyOfTheThing() {
        final var thing = entry(1);
        final var command = StructureCommand.removal(store, OutlineKind.Object, 0, thing, "Delete");

        thing.addProperty("v", 100);
        command.revert();

        assertEquals(List.of("insert Object#0:1"), calls);
    }

    @Test
    void testRedoAfterUndoDoesTheSameAgain() {
        final var command = StructureCommand.insertion(store, OutlineKind.Water, 1, entry(5), "Create");

        command.execute();
        command.revert();
        command.execute();

        assertEquals(List.of("insert Water#1:5", "remove Water#1", "insert Water#1:5"), calls);
    }

    @Test
    void testStructureCommandsAreNeverMerged() {
        final var command = StructureCommand.insertion(store, OutlineKind.Object, 0, entry(1), "Create");

        assertNull(command.mergeKey());
        assertNull(command.merge(command));
    }
}
