package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.services.ResourceService;

final class ArchiveUtilTest {
    @Test
    void testArchiveFoldersMatchEngineLookupPaths() {
        assertEquals("soundsources", GroupType.AudioSource.getArchiveFolder(), "Sound sources are loaded from /soundsources");
        assertEquals("cubemaps", GroupType.CubeMap.getArchiveFolder(), "Cube maps are loaded from /cubemaps");
        assertEquals("gridmeshes", GroupType.GridMesh.getArchiveFolder(), "Grid meshes are loaded from /gridmeshes");

        for (final var type : GroupType.values())
            assertTrue(type.getArchiveFolder().matches("[a-z]+"), type + " archive folder must be a lowercase word");
    }

    @Test
    void testResourcePathsKeepFolderAndFileNames(@TempDir final Path directory) throws IOException {
        final var archive = directory.resolve("test.vea");
        try (final var out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(archive))))) {
            /* Same layout as the editor writes: group name, then the group root node with its children */
            out.writeUTF(GroupType.CubeMap.name());
            writeNode(out, GroupType.CubeMap.getArchiveFolder(), null, 1);
            writeNode(out, "Sky.json", "{}", 0);
        }

        final var resources = ArchiveUtil.load(archive.toFile());

        assertEquals(1, resources.size(), "Archive must contain one resource");
        final ResourceService.Resource resource = resources.getFirst();
        assertEquals("/cubemaps/Sky.json", resource.resourcePath(), "Resource path must match the engine lookup path");
        assertEquals(GroupType.CubeMap, resource.type(), "Resource type must be read");
        assertEquals("{}", new String(resource.data(), StandardCharsets.UTF_8), "Resource data must be read");
    }

    private static void writeNode(final DataOutputStream out, final String name, final String data, final int childrenCount) throws IOException {
        out.writeUTF(name);
        out.writeBoolean(data != null);
        if (data != null) {
            final var bytes = data.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        out.writeInt(childrenCount);
    }
}
