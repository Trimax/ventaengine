package io.github.trimax.venta.engine.utils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;

@UtilityClass
public final class ArchiveUtil {
    @SneakyThrows
    public void load(@NonNull final File file, @NonNull final BiConsumer<String, byte[]> resourceLoader) {
        try (var in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))) {
            loadNode(in, resourceLoader, "");
        }
    }

    @SneakyThrows
    private void loadNode(final DataInputStream in, final BiConsumer<String, byte[]> resourceLoader, @NonNull final String path) {
        final var name = in.readUTF();

        final var hasReference = in.readBoolean();
        if (hasReference) {
            final var length = in.readInt();
            resourceLoader.accept(String.format("%s/%s", path, name).substring("/Root".length()), in.readNBytes(length));
        }

        final var childrenCount = in.readInt();
        for (int i = 0; i < childrenCount; i++)
            loadNode(in, resourceLoader, String.format("%s/%s", path, name));
    }
}
