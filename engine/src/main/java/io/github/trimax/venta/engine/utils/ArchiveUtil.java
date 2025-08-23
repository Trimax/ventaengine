package io.github.trimax.venta.engine.utils;

import io.github.trimax.venta.engine.enums.DataType;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

@UtilityClass
public final class ArchiveUtil {
    @SneakyThrows
    public void load(@NonNull final File file, @NonNull final TriConsumer<String, DataType, byte[]> resourceLoader) {
        try (var in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))) {
            loadNode(in, resourceLoader, "");
        }
    }

    @SneakyThrows
    private void loadNode(final DataInputStream in, final TriConsumer<String, DataType, byte[]> resourceLoader, @NonNull final String path) {
        final var name = in.readUTF();
        final var type = DataType.of(in.readUTF());

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
