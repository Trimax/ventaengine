package io.github.trimax.venta.studio.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Renames a file or a folder of a project and the places that name it: the files of the project (scenes, things, materials, ...) that have its name in
 * a field of its group are changed, and nothing else in them is. What the change would be is known before it is made.
 */
@UtilityClass
public final class RenameUtil {
    /**
     * A file whose text changes.
     *
     * @param file   the file
     * @param before its text now
     * @param after  its text with the new name
     * @param count  how many places of the text name the file
     */
    public record Change(Path file, String before, String after, int count) {
    }

    /**
     * What a rename does.
     *
     * @param from    the file that is renamed
     * @param to      where it is after
     * @param changes the files that name it and are changed
     */
    public record Plan(Path from, Path to, List<Change> changes) {
        /** How many places of the project name the file */
        public int references() {
            return changes.stream().mapToInt(Change::count).sum();
        }
    }

    /**
     * What renaming a file would do.
     *
     * @param newName the name of the file in its folder, without a path
     * @throws IllegalArgumentException when the name is not a name of a file, or the folder has a file with it already
     */
    public Plan plan(@NonNull final Path project, @NonNull final Path from, @NonNull final String newName) {
        final var name = newName.trim();
        if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\"))
            throw new IllegalArgumentException("`" + newName + "` is not a name of a file");

        if (!(Files.isRegularFile(from) || Files.isDirectory(from)) || !from.toAbsolutePath().normalize().startsWith(project.toAbsolutePath().normalize())
                || from.toAbsolutePath().normalize().equals(project.toAbsolutePath().normalize()))
            throw new IllegalArgumentException(from + " is not a file or a folder of the project");

        final var group = ProjectUtil.groupOf(project, from);
        if (group != null && from.toAbsolutePath().normalize().equals(project.resolve(group.getArchiveFolder()).toAbsolutePath().normalize()))
            throw new IllegalArgumentException("The folder of " + group.getDisplayName().toLowerCase() + " is where the engine looks for them, it keeps its name");

        if (name.equals(from.getFileName().toString()))
            throw new IllegalArgumentException("The file is called so already");

        final var to = from.resolveSibling(name);
        if (Files.exists(to) && !isSameFile(from, to))
            throw new IllegalArgumentException("The folder has a file `" + name + "` already");

        final var changes = new ArrayList<Change>();
        if (group != null) {
            final var folder = project.resolve(group.getArchiveFolder());
            final var oldName = folder.relativize(from).toString().replace('\\', '/');
            final var newPath = folder.relativize(to).toString().replace('\\', '/');

            // The files inside of a renamed folder are named by the path that starts with the name of the folder
            final var renamesFolder = Files.isDirectory(from);
            for (final var file : jsonFiles(project)) {
                final var text = read(file);
                final var places = renamesFolder ? ReferenceScanUtil.inside(text, group, oldName + "/") : ReferenceScanUtil.to(text, group, oldName);
                if (!places.isEmpty())
                    changes.add(new Change(file, text, renamesFolder ? ReferenceScanUtil.replaceEach(text, places, value -> newPath + value.substring(oldName.length())) : ReferenceScanUtil.replace(text, places, newPath), places.size()));
            }
        }

        return new Plan(from, to, changes);
    }

    /**
     * Does a rename: the files that name the file get its new name, then the file is moved. When something fails, the files are as they
     * were.
     */
    public void apply(@NonNull final Plan plan) throws IOException {
        final var written = new ArrayList<Change>();

        try {
            for (final var change : plan.changes()) {
                write(change.file(), change.after());
                written.add(change);
            }

            Files.move(plan.from(), plan.to(), StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException | RuntimeException exception) {
            for (final var change : written) {
                try {
                    write(change.file(), change.before());
                } catch (final IOException again) {
                    exception.addSuppressed(again);
                }
            }

            throw exception;
        }
    }

    private void write(final Path file, final String text) throws IOException {
        final var temporary = file.resolveSibling(file.getFileName() + ".renaming");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /** The files of the project that can name a file: the files of JSON, outside of the folders that are not a part of the project */
    private List<Path> jsonFiles(final Path project) {
        try (final Stream<Path> found = Files.walk(project)) {
            return found.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".json"))
                    .filter(file -> project.relativize(file).getNameCount() == 1 || !isOutside(project.relativize(file).getParent()))
                    .sorted()
                    .toList();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /** Whether a folder (from the project) is one that is not a part of it: it, or a folder it is in, starts with a dot or is a build output */
    private boolean isOutside(final Path folder) {
        for (final var part : folder) {
            final var name = part.toString();
            if (name.startsWith(".") || name.equals("target") || name.equals("build"))
                return true;
        }

        return false;
    }

    private boolean isSameFile(final Path first, final Path second) {
        try {
            return Files.isSameFile(first, second);
        } catch (final IOException exception) {
            return false;
        }
    }

    private String read(final Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
