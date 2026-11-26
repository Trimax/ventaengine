package io.github.trimax.venta.studio.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A line of the log of the engine.
 *
 * @param level  how important it is: TRACE, DEBUG, INFO, WARN, ERROR or FATAL
 * @param text   what happened
 * @param millis when it happened
 */
public record LogLine(String level, String text, long millis) {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /** Whether the line is as important as the level, or more */
    public boolean isAtLeast(final String minimal) {
        return rank(level) >= rank(minimal);
    }

    @Override
    public String toString() {
        return TIME.format(Instant.ofEpochMilli(millis)) + "  " + String.format("%-5s", level) + "  " + text;
    }

    private static int rank(final String level) {
        return switch (level) {
            case "TRACE" -> 0;
            case "DEBUG" -> 1;
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 5;
        };
    }
}
