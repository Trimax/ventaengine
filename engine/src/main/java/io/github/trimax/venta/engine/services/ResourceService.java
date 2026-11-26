package io.github.trimax.venta.engine.services;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.exceptions.ResourceNotFoundException;
import io.github.trimax.venta.engine.utils.ArchiveUtil;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceService {
    private final Map<String, Resource> resources = new HashMap<>();

    /**
     * The folder of a project (an editor opens one): its files come before the archives and the classpath, with the same layout
     * as the resources of an application: {@code scenes/beach.json}, {@code textures/sand.png}.
     */
    private Path projectRoot;

    /**
     * Makes the files of a folder the resources of the application: they are found first, and read from the disk on every request, so
     * a file that is changed is loaded changed.
     */
    public void useProject(@NonNull final Path root) {
        if (!Files.isDirectory(root))
            throw new IllegalArgumentException("The project folder doesn't exist: " + root);

        projectRoot = root.toAbsolutePath().normalize();
        log.info("Project folder {}", projectRoot);
    }

    /** The folder of the project, empty when the resources are in the archives and the classpath only */
    public Optional<Path> getProject() {
        return Optional.ofNullable(projectRoot);
    }

    public void loadArchive(@NonNull final String archiveFile) {
        resources.putAll(StreamEx.of(ArchiveUtil.load(new File(archiveFile))).toMap(Resource::resourcePath, Function.identity()));
    }

    public byte[] getAsBytes(@NonNull final String path) {
        return get(path, Function.identity(), () -> ResourceUtil.loadAsBytes(path));
    }

    public String getAsString(@NonNull final String path) {
        return get(path, bytes -> new String(bytes, StandardCharsets.UTF_8),
                () -> ResourceUtil.loadAsString(path));
    }

    public <O> O getAsObject(@NonNull final String path, @NonNull final Class<O> objectClass) {
        return Optional.ofNullable(getAsString(path))
                .map(string -> ParsingUtil.asObject(string, objectClass))
                .orElse(null);
    }

    /** The file of the project that is the resource, null when there is none: a path that leaves the folder is not a resource */
    private byte[] readProjectFile(final String path) {
        if (projectRoot == null)
            return null;

        final var file = projectRoot.resolve(path.startsWith("/") ? path.substring(1) : path).normalize();
        if (!file.startsWith(projectRoot) || !Files.isRegularFile(file))
            return null;

        try {
            return Files.readAllBytes(file);
        } catch (final IOException exception) {
            throw new ResourceNotFoundException(path);
        }
    }

    private <T> T get(@NonNull final String path,
                      @NonNull final Function<byte[], T> mapper,
                      @NonNull final Supplier<T> alternative) {
        final var projectFile = readProjectFile(path);
        if (projectFile != null)
            return mapper.apply(projectFile);

        return Optional.of(path)
                .map(resources::get)
                .map(Resource::data)
                .map(mapper)
                .orElseGet(alternative);
    }

    public record Resource(File archiveFile, String resourcePath, GroupType type, byte[] data) {
    }
}
