package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.services.ResourceService;
import one.util.streamex.StreamEx;

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
        try (final var writer = new ArchiveTestWriter(archive)) {
            writer.group(GroupType.CubeMap, 1).file("Sky.json", "{}");
        }

        final var resources = ArchiveUtil.load(archive.toFile());

        assertEquals(1, resources.size(), "Archive must contain one resource");
        final ResourceService.Resource resource = resources.getFirst();
        assertEquals("/cubemaps/Sky.json", resource.resourcePath(), "Resource path must match the engine lookup path");
        assertEquals(GroupType.CubeMap, resource.type(), "Resource type must be read");
        assertEquals("{}", new String(resource.data(), StandardCharsets.UTF_8), "Resource data must be read");
    }

    @Test
    void testAllGroupsAreReadUntilTheEndOfTheArchive(@TempDir final Path directory) throws IOException {
        final var archive = directory.resolve("test.vea");
        try (final var writer = new ArchiveTestWriter(archive)) {
            writer.group(GroupType.Texture, 2)
                    .file("a.png", "a")
                    .folder("nested", 1)
                    .folder("deeper", 1)
                    .file("b.png", "b");
            writer.group(GroupType.AudioSource, 0);
            writer.group(GroupType.Material, 1).file("stone.json", "stone");
        }

        final Map<String, String> resources = StreamEx.of(ArchiveUtil.load(archive.toFile()))
                .toMap(ResourceService.Resource::resourcePath, resource -> new String(resource.data(), StandardCharsets.UTF_8));

        assertEquals(Map.of(
                "/textures/a.png", "a",
                "/textures/nested/deeper/b.png", "b",
                "/materials/stone.json", "stone"), resources, "Resources of all groups, including those after an empty group, must be read");
    }

    @Test
    void testEmptyArchive(@TempDir final Path directory) throws IOException {
        final var archive = directory.resolve("empty.vea");
        try (final var _ = new ArchiveTestWriter(archive)) {
            // No groups
        }

        assertTrue(ArchiveUtil.load(archive.toFile()).isEmpty(), "Empty archive must have no resources");
    }
}
