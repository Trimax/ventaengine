package io.github.trimax.venta.studio.model.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import io.github.trimax.venta.studio.model.outline.OutlineKind;

final class ReplaceEntryCommandTest {
    private final List<String> writes = new ArrayList<>();
    private final ReplaceEntryCommand.Writer writer = (kind, index, entry) -> writes.add(kind + "#" + index + ":" + entry.get("v").getAsInt());

    private static JsonObject entry(final int value) {
        final var entry = new JsonObject();
        entry.addProperty("v", value);
        return entry;
    }

    private ReplaceEntryCommand command(final int index, final int from, final int to, final String field) {
        return new ReplaceEntryCommand(writer, OutlineKind.Object, index, entry(from), entry(to), "Move A", field);
    }

    @Test
    void testExecuteWritesTheNewThingAndRevertTheOldOne() {
        final var command = command(2, 1, 5, "position");

        command.execute();
        command.revert();

        assertEquals(List.of("Object#2:5", "Object#2:1"), writes);
        assertEquals("Move A", command.label());
    }

    @Test
    void testCommandKeepsCopiesOfTheThings() {
        final var before = entry(1);
        final var after = entry(2);
        final var command = new ReplaceEntryCommand(writer, OutlineKind.Light, 0, before, after, "Edit", "name");

        before.addProperty("v", 100);
        after.addProperty("v", 200);
        command.execute();
        command.revert();

        assertEquals(List.of("Light#0:2", "Light#0:1"), writes, "A change of what was given doesn't change the command");
    }

    @Test
    void testStepsOfTheSameThingAndFieldAreMerged() {
        final var merged = command(0, 1, 2, "position").merge(command(0, 2, 7, "position"));

        assertNotNull(merged);
        merged.execute();
        merged.revert();

        assertEquals(List.of("Object#0:7", "Object#0:1"), writes, "It goes from the first state to the last one");
    }

    @Test
    void testStepsOfAnotherThingOrFieldAreNotMerged() {
        final var command = command(0, 1, 2, "position");

        assertNull(command.merge(command(1, 2, 3, "position")), "Another thing");
        assertNull(command.merge(command(0, 2, 3, "angles")), "Another field");
        assertNotEquals(command.mergeKey(), command(0, 1, 2, "angles").mergeKey());
        assertEquals(command.mergeKey(), command(0, 5, 6, "position").mergeKey());
    }

    @Test
    void testCommandIsNotMergedWithWhatIsNotAReplacement() {
        final var other = new Command() {
            @Override
            public String label() {
                return "x";
            }

            @Override
            public void execute() {
            }

            @Override
            public void revert() {
            }
        };

        assertNull(command(0, 1, 2, "position").merge(other));
    }
}
