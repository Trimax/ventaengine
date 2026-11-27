package io.github.trimax.venta.studio.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * What the files of a project are on the disk, to tell which of them were changed by another program: the time each was changed.
 */
@UtilityClass
public final class DiskUtil {
    /** The files of the project (not the ones in hidden folders or in the output of a build) with the time they were changed (milliseconds) */
    public Map<Path, Long> snapshot(@NonNull final Path project) {
        final var files = new HashMap<Path, Long>();

        try (final Stream<Path> found = Files.walk(project)) {
            found.filter(Files::isRegularFile)
                    .filter(file -> !isOutside(project.relativize(file)))
                    .forEach(file -> files.put(file, file.toFile().lastModified()));
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }

        return files;
    }

    /** The files that are in the later snapshot and are new or were changed since the earlier one, by path */
    public List<Path> changed(@NonNull final Map<Path, Long> before, @NonNull final Map<Path, Long> after) {
        return after.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(before.get(entry.getKey())))
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /** Whether a path (from the project) is in a folder that is not a part of the project: a hidden one, or a build output */
    private boolean isOutside(final Path relative) {
        for (int index = 0; index < relative.getNameCount() - 1; index++) {
            final var name = relative.getName(index).toString();
            if (name.startsWith(".") || name.equals("target") || name.equals("build"))
                return true;
        }

        return false;
    }
}
