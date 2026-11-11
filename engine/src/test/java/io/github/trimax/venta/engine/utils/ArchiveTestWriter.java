package io.github.trimax.venta.engine.utils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import io.github.trimax.venta.engine.enums.GroupType;

/** Writes archives in the format the editor builds: per group its name, then the tree of folders and files */
public final class ArchiveTestWriter implements AutoCloseable {
    private final DataOutputStream out;

    public ArchiveTestWriter(final Path archive) throws IOException {
        this.out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(archive))));
    }

    public ArchiveTestWriter group(final GroupType type, final int childrenCount) throws IOException {
        out.writeUTF(type.name());
        return node(type.getArchiveFolder(), null, childrenCount);
    }

    public ArchiveTestWriter folder(final String name, final int childrenCount) throws IOException {
        return node(name, null, childrenCount);
    }

    public ArchiveTestWriter file(final String name, final String data) throws IOException {
        return node(name, data, 0);
    }

    private ArchiveTestWriter node(final String name, final String data, final int childrenCount) throws IOException {
        out.writeUTF(name);
        out.writeBoolean(data != null);
        if (data != null) {
            final var bytes = data.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        out.writeInt(childrenCount);
        return this;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
