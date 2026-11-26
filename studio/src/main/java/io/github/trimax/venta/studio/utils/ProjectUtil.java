package io.github.trimax.venta.studio.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import io.github.trimax.venta.engine.enums.GroupType;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The folder of a project is laid out like the resources of an application: the folder of a group of resources is where the engine
 * looks for them ({@code scenes/}, {@code textures/}, {@code surfaces/terrain/}).
 */
@UtilityClass
public final class ProjectUtil {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "bmp", "gif");
    private static final Set<String> TEXT_EXTENSIONS = Set.of("json", "txt", "glsl", "obj", "mtl", "md", "xml", "csv", "properties");

    /** How the contents of a file are shown */
    public enum Preview {
        Image, Text, Binary
    }

    /** The group of resources a path belongs to, null for what is outside of the folders of the groups */
    public GroupType groupOf(@NonNull final Path project, @NonNull final Path path) {
        final var relative = project.relativize(path).toString().replace('\\', '/');

        GroupType found = null;
        for (final var group : GroupType.values()) {
            final var folder = group.getArchiveFolder();
            final var inside = relative.equals(folder) || relative.startsWith(folder + "/");

            // The longest folder wins: surfaces/terrain is not surfaces
            if (inside && (found == null || folder.length() > found.getArchiveFolder().length()))
                found = group;
        }

        return found;
    }

    /** The folders inside a folder, by name; the ones that start with a dot or are the output of a build are not a part of a project */
    public List<Path> listFolders(@NonNull final Path folder) {
        return list(folder, Files::isDirectory);
    }

    /** The files inside a folder, by name */
    public List<Path> listFiles(@NonNull final Path folder) {
        return list(folder, Files::isRegularFile);
    }

    public Preview previewOf(@NonNull final Path file) {
        final var name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        final var extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";

        if (IMAGE_EXTENSIONS.contains(extension))
            return Preview.Image;

        return TEXT_EXTENSIONS.contains(extension) ? Preview.Text : Preview.Binary;
    }

    /** The name of a scene of a project as the engine knows it: the path inside the folder of the scenes, null for a file that is not a scene */
    public String sceneNameOf(@NonNull final Path project, @NonNull final Path file) {
        final var relative = project.relativize(file).toString().replace('\\', '/');
        final var prefix = GroupType.Scene.getArchiveFolder() + "/";

        return relative.startsWith(prefix) && relative.endsWith(".json") ? relative.substring(prefix.length()) : null;
    }

    private List<Path> list(final Path folder, final java.util.function.Predicate<Path> filter) {
        try (final Stream<Path> children = Files.list(folder)) {
            return children.filter(filter)
                    .filter(path -> !isHidden(path))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        } catch (final IOException exception) {
            return List.of();
        }
    }

    private boolean isHidden(final Path path) {
        final var name = path.getFileName().toString();
        return name.startsWith(".") || (Files.isDirectory(path) && (name.equals("target") || name.equals("build")));
    }
}
