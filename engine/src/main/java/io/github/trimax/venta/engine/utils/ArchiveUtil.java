package io.github.trimax.venta.engine.utils;

import io.github.trimax.venta.engine.enums.ResourceType;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@UtilityClass
public final class ArchiveUtil {
    public List<ResourceService.Resource> load(@NonNull final File archive) {
        final List<ResourceService.Resource> resources = new ArrayList<>();
        load(archive, (resourcePath, resourceType, data) -> {
            resources.add(new ResourceService.Resource(archive, resourcePath, resourceType, data));
            log.info("Resource {} loaded from archive {}", resourcePath, archive.getAbsolutePath());
        });

        return resources;
    }

    @SneakyThrows
    private void load(@NonNull final File archive, @NonNull final TriConsumer<String, ResourceType, byte[]> resourceLoader) {
        try (var in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(archive))))) {
            loadNode(in, resourceLoader, "");
        }
    }

    @SneakyThrows
    private void loadNode(final DataInputStream in, final TriConsumer<String, ResourceType, byte[]> resourceLoader, @NonNull final String path) {
        final var name = in.readUTF();
        final var type = ResourceType.of(in.readUTF());

        final var hasReference = in.readBoolean();
        if (hasReference) {
            final var length = in.readInt();
            resourceLoader.accept(String.format("%s/%s", path, name).substring("/Root".length()), type, in.readNBytes(length));
        }

        final var childrenCount = in.readInt();
        for (int i = 0; i < childrenCount; i++)
            loadNode(in, resourceLoader, String.format("%s/%s", path, name));
    }
}
