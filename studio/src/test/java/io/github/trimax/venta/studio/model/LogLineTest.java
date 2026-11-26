package io.github.trimax.venta.studio.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class LogLineTest {
    @Test
    void testLineIsAsImportantAsItsLevel() {
        final var warning = new LogLine("WARN", "something", 0);

        assertTrue(warning.isAtLeast("DEBUG"));
        assertTrue(warning.isAtLeast("INFO"));
        assertTrue(warning.isAtLeast("WARN"));
        assertFalse(warning.isAtLeast("ERROR"));
    }

    @Test
    void testFatalIsTheMostImportant() {
        assertTrue(new LogLine("FATAL", "x", 0).isAtLeast("ERROR"));
        assertFalse(new LogLine("TRACE", "x", 0).isAtLeast("DEBUG"));
    }

    @Test
    void testLineShowsTheLevelAndTheText() {
        final var text = new LogLine("INFO", "Scene is open", 0).toString();

        assertTrue(text.contains("INFO"));
        assertTrue(text.endsWith("Scene is open"));
    }
}
