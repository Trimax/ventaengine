package io.github.trimax.venta.engine.services;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ResourceType;
import io.github.trimax.venta.engine.utils.ArchiveUtil;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceService {
    private final Map<String, Resource> resources = new HashMap<>();

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

    private <T> T get(@NonNull final String path,
                      @NonNull final Function<byte[], T> mapper,
                      @NonNull final Supplier<T> alternative) {
        return Optional.of(path)
                .map(resources::get)
                .map(Resource::data)
                .map(mapper)
                .orElseGet(alternative);
    }

    public record Resource(File archiveFile, String resourcePath, ResourceType type, byte[] data) {
    }
}
