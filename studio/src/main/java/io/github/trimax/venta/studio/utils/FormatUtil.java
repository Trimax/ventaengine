package io.github.trimax.venta.studio.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Texts for the panels: sizes of files the way people read them.
 */
@UtilityClass
public final class FormatUtil {
    private static final long KILOBYTE = 1024;
    private static final long MEGABYTE = 1024 * KILOBYTE;

    /**
     * The size as the closest unit: {@code 512 B}, {@code 1.5 KB}, {@code 2.0 MB}.
     */
    public String size(final long bytes) {
        if (bytes < KILOBYTE)
            return bytes + " B";

        return bytes < MEGABYTE
                ? String.format(Locale.ROOT, "%.1f KB", bytes / (double) KILOBYTE)
                : String.format(Locale.ROOT, "%.1f MB", bytes / (double) MEGABYTE);
    }

    /** The size of a file, an empty text for a file that can't be read */
    public String size(@NonNull final Path file) {
        try {
            return size(Files.size(file));
        } catch (final IOException exception) {
            return "";
        }
    }

    /**
     * A number for a field: at most four decimals, none that are zeros: {@code 2.5}, {@code 0}, {@code -1.25}.
     */
    public String number(final float value) {
        final var text = String.format(Locale.ROOT, "%.4f", value);
        final var trimmed = text.replaceAll("0+$", "").replaceAll("\\.$", "");

        return trimmed.equals("-0") || trimmed.isEmpty() ? "0" : trimmed;
    }
}
