package io.github.trimax.venta.studio.utils;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Where a file that is brought into a project goes: the folder that is chosen, or (for the folder of the whole project) the folder that
 * the engine looks for a file of its kind in; and under which name, so a file of the project is never replaced.
 */
@UtilityClass
public final class ImportUtil {
    /** The folder of the project that a file goes to by how its name ends, when no folder is chosen */
    private static final Map<String, String> FOLDERS = Map.ofEntries(
            Map.entry("png", "textures"),
            Map.entry("jpg", "textures"),
            Map.entry("jpeg", "textures"),
            Map.entry("hdr", "textures"),
            Map.entry("obj", "meshes"),
            Map.entry("mtl", "meshes"),
            Map.entry("ogg", "sounds"),
            Map.entry("ttf", "fonts"),
            Map.entry("glsl", "shaders"));

    /**
     * The folder a file is put in.
     *
     * @param folder the folder that is chosen in the project, null for none
     * @return the chosen folder; when the whole project is chosen (or nothing), the folder of the kind of the file, and the project when
     *     the kind is not known
     */
    public Path folderFor(@NonNull final Path project, final Path folder, @NonNull final String name) {
        if (folder != null && !folder.equals(project))
            return folder;

        final var dot = name.lastIndexOf('.');
        final var kind = dot < 0 ? null : FOLDERS.get(name.substring(dot + 1).toLowerCase(Locale.ROOT));

        return kind == null ? project : project.resolve(kind);
    }

    /**
     * The name a file has in a folder: its own, or with the next number when the folder has a file of that name ({@code wall.png},
     * {@code wall 2.png}, {@code wall 3.png}).
     *
     * @param exists tells whether a path is taken
     */
    public String uniqueName(@NonNull final Path folder, @NonNull final String name, @NonNull final Predicate<Path> exists) {
        if (!exists.test(folder.resolve(name)))
            return name;

        final var dot = name.lastIndexOf('.');
        final var stem = dot <= 0 ? name : name.substring(0, dot);
        final var extension = dot <= 0 ? "" : name.substring(dot);

        var number = 2;
        while (exists.test(folder.resolve(stem + " " + number + extension)))
            number++;

        return stem + " " + number + extension;
    }
}
