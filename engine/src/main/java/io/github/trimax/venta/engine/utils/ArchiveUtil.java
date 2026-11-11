package io.github.trimax.venta.engine.utils;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.*;
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
    private void load(@NonNull final File archive, @NonNull final TriConsumer<String, GroupType, byte[]> resourceLoader) {
        try (var stream = new PushbackInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(archive))));
                var in = new DataInputStream(stream)) {
            while (hasMoreData(stream))
                loadNode(in, resourceLoader, "", GroupType.of(in.readUTF()));
        }
    }

    /** GZIPInputStream.available() reports 1 until a read reaches the end, so the end is detected by reading ahead */
    private boolean hasMoreData(@NonNull final PushbackInputStream stream) throws IOException {
        final var next = stream.read();
        if (next == -1)
            return false;

        stream.unread(next);
        return true;
    }

    @SneakyThrows
    private void loadNode(final DataInputStream in, final TriConsumer<String, GroupType, byte[]> resourceLoader, @NonNull final String path, final GroupType type) {
        final var name = in.readUTF();

        final var hasReference = in.readBoolean();
        if (hasReference) {
            final var length = in.readInt();
            resourceLoader.accept(String.format("%s/%s", path, name), type, in.readNBytes(length));
        }

        final var childrenCount = in.readInt();
        for (int i = 0; i < childrenCount; i++)
            loadNode(in, resourceLoader, String.format("%s/%s", path, name), type);
    }
}
