package io.github.trimax.venta.studio.model.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

final class ReplaceSectionCommandTest {
    private final List<String> writes = new ArrayList<>();
    private final ReplaceSectionCommand.Writer writer = (section, value) -> writes.add(section + ":" + value.get("v").getAsInt());

    private static JsonObject value(final int number) {
        final var value = new JsonObject();
        value.addProperty("v", number);
        return value;
    }

    private ReplaceSectionCommand command(final String section, final int from, final int to, final String field) {
        return new ReplaceSectionCommand(writer, section, value(from), value(to), "Change fog", field);
    }

    @Test
    void testExecuteWritesTheNewValueAndRevertTheOldOne() {
        final var command = command("Fog", 1, 5, "density");

        command.execute();
        command.revert();

        assertEquals(List.of("Fog:5", "Fog:1"), writes);
        assertEquals("Change fog", command.label());
    }

    @Test
    void testStepsOfTheSameFieldAreMergedIntoOne() {
        final var merged = command("Fog", 1, 2, "density").merge(command("Fog", 2, 9, "density"));

        assertNotNull(merged);
        merged.revert();
        merged.execute();

        assertEquals(List.of("Fog:1", "Fog:9"), writes, "From the first value to the last one");
    }

    @Test
    void testOtherFieldOrOtherSectionIsNotMerged() {
        assertNull(command("Fog", 1, 2, "density").merge(command("Fog", 2, 3, "falloff")));
        assertNull(command("Fog", 1, 2, "density").merge(command("Bloom", 2, 3, "density")));
    }
}
