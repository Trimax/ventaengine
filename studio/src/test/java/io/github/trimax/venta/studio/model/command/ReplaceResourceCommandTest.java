package io.github.trimax.venta.studio.model.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

final class ReplaceResourceCommandTest {
    private final List<String> writes = new ArrayList<>();
    private final ReplaceResourceCommand.Writer writer = (path, value) -> writes.add(path + ":" + value.get("v").getAsInt());

    private static JsonObject value(final int number) {
        final var value = new JsonObject();
        value.addProperty("v", number);
        return value;
    }

    private ReplaceResourceCommand command(final String path, final int from, final int to, final String field) {
        return new ReplaceResourceCommand(writer, path, value(from), value(to), "Change spot.json", field);
    }

    @Test
    void testExecuteWritesTheNewValueAndRevertTheOldOne() {
        final var command = command("lights/spot.json", 1, 5, "range");

        command.execute();
        command.revert();

        assertEquals(List.of("lights/spot.json:5", "lights/spot.json:1"), writes);
    }

    @Test
    void testStepsOfTheSameFieldOfTheSameFileAreMerged() {
        final var merged = command("lights/spot.json", 1, 2, "range").merge(command("lights/spot.json", 2, 9, "range"));

        assertNotNull(merged);
        merged.revert();
        merged.execute();

        assertEquals(List.of("lights/spot.json:1", "lights/spot.json:9"), writes);
    }

    @Test
    void testOtherFieldOrOtherFileIsNotMerged() {
        assertNull(command("lights/spot.json", 1, 2, "range").merge(command("lights/spot.json", 2, 3, "intensity")));
        assertNull(command("lights/spot.json", 1, 2, "range").merge(command("lights/point.json", 2, 3, "range")));
    }
}
