package io.github.trimax.venta.editor.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.ArchiveUtil;
import javafx.scene.control.TreeItem;
import one.util.streamex.StreamEx;

final class ArchiveFileUtilTest {
    @Test
    void testArchiveBuiltByEditorIsLoadedByEngine(@TempDir final Path directory) throws IOException {
        final var groups = createGroups(directory);
        final var archive = directory.resolve("test.vea");
        try (final var out = Files.newOutputStream(archive)) {
            ArchiveFileUtil.build(out, groups);
        }

        final Map<String, String> resources = StreamEx.of(ArchiveUtil.load(archive.toFile()))
                .toMap(ResourceService.Resource::resourcePath, resource -> new String(resource.data(), StandardCharsets.UTF_8));

        assertEquals(Map.of(
                "/textures/Grass.png", "grass",
                "/textures/terrain/Rock.png", "rock",
                "/cubemaps/sky.json", "{\"sky\": true}"), resources, "Resources must be loaded by the paths the engine looks them up");
    }

    @Test
    void testProjectJsonRoundTrip(@TempDir final Path directory) throws IOException {
        final var groups = createGroups(directory);

        final var loaded = ArchiveFileUtil.fromJson(ArchiveFileUtil.toJson(groups, directory), directory);

        assertEquals(groups.keySet(), loaded.keySet(), "All groups must be saved and loaded");
        for (final var type : groups.keySet())
            assertEquals(ArchiveFileUtil.toNode(groups.get(type)), ArchiveFileUtil.toNode(loaded.get(type)), type + " tree must be restored");
    }

    @Test
    void testGroupsMissingInProjectJsonAreNotLoaded() {
        final var json = "{\"id\": \"test\", \"groups\": {\"Texture\": {\"name\": \"Textures\", "
                + "\"value\": {\"type\": \"Group\", \"name\": \"Textures\", \"reference\": \"Texture\"}, \"children\": []}}}";

        final var loaded = ArchiveFileUtil.fromJson(json, Path.of("."));

        assertEquals(1, loaded.size(), "Only the group present in the file must be loaded");
        assertTrue(loaded.containsKey(GroupType.Texture), "Texture group must be loaded");
    }

    @Test
    void testProjectJsonStoresRelativePaths(@TempDir final Path directory) throws IOException {
        final var groups = createGroups(directory);

        final var json = ArchiveFileUtil.toJson(groups, directory);

        assertFalse(json.contains(directory.toAbsolutePath().toString()), "Project directory must not be stored in the JSON");
        assertTrue(json.contains("Grass.png"), "File references must be stored");
    }

    @Test
    void testMovedProjectResolvesFilesInTheNewLocation(@TempDir final Path directory) throws IOException {
        final var original = Files.createDirectories(directory.resolve("original"));
        final var json = ArchiveFileUtil.toJson(createGroups(original), original);

        final var moved = directory.resolve("moved");
        Files.move(original, moved);
        final var loaded = ArchiveFileUtil.fromJson(json, moved);

        assertTrue(ArchiveFileUtil.findMissingFiles(loaded).isEmpty(), "All files must be found in the moved project");
        final var grass = loaded.get(GroupType.Texture).getChildren().getFirst().getValue();
        assertTrue(Path.of(grass.reference()).startsWith(moved), "Reference must point into the moved project, got " + grass.reference());
    }

    @Test
    void testGroupReferencesAreNotChanged(@TempDir final Path directory) throws IOException {
        final var loaded = ArchiveFileUtil.fromJson(ArchiveFileUtil.toJson(createGroups(directory), directory), directory);

        assertEquals(GroupType.Texture.name(), loaded.get(GroupType.Texture).getValue().reference(), "Group reference is the group type, not a path");
    }

    @Test
    void testMissingFilesAreReported(@TempDir final Path directory) throws IOException {
        final var groups = createGroups(directory);
        groups.get(GroupType.Texture).getChildren().add(new TreeItem<>(Resource.asResource("Lost.png", directory.resolve("lost.png").toString())));

        final var missing = ArchiveFileUtil.findMissingFiles(groups);

        assertEquals(1, missing.size(), "Only the missing file must be reported");
        assertEquals("Lost.png", missing.getFirst().name(), "Missing file");
    }

    private static Map<GroupType, TreeItem<Resource>> createGroups(final Path directory) throws IOException {
        final var textures = new TreeItem<>(Resource.asGroup(GroupType.Texture));
        textures.getChildren().add(file(directory, "Grass.png", "grass"));
        final var terrain = new TreeItem<>(Resource.asFolder("terrain"));
        terrain.getChildren().add(file(directory, "Rock.png", "rock"));
        textures.getChildren().add(terrain);

        final var cubemaps = new TreeItem<>(Resource.asGroup(GroupType.CubeMap));
        cubemaps.getChildren().add(file(directory, "sky.json", "{\"sky\": true}"));

        final var groups = new LinkedHashMap<GroupType, TreeItem<Resource>>();
        groups.put(GroupType.AudioSource, new TreeItem<>(Resource.asGroup(GroupType.AudioSource)));
        groups.put(GroupType.Texture, textures);
        groups.put(GroupType.CubeMap, cubemaps);
        return groups;
    }

    private static TreeItem<Resource> file(final Path directory, final String name, final String content) throws IOException {
        final var file = Files.writeString(Files.createTempDirectory(directory, "source").resolve(name), content);
        return new TreeItem<>(Resource.asResource(name, file.toAbsolutePath().toString()));
    }
}
