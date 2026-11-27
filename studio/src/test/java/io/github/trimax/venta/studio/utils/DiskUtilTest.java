package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DiskUtilTest {
    @TempDir
    private Path project;

    @BeforeEach
    void createProject() throws IOException {
        Files.createDirectories(project.resolve("scenes"));
        Files.createDirectories(project.resolve(".cache"));
        Files.createDirectories(project.resolve("target"));
        Files.writeString(project.resolve("scenes/a.json"), "{}");
        Files.writeString(project.resolve("readme.txt"), "x");
        Files.writeString(project.resolve(".cache/hidden.json"), "{}");
        Files.writeString(project.resolve("target/out.json"), "{}");
    }

    @Test
    void testSnapshotHasTheFilesOfTheProjectOnly() {
        final var files = DiskUtil.snapshot(project).keySet();

        assertTrue(files.contains(project.resolve("scenes/a.json")));
        assertTrue(files.contains(project.resolve("readme.txt")));
        assertFalse(files.contains(project.resolve(".cache/hidden.json")), "A hidden folder is not a part of the project");
        assertFalse(files.contains(project.resolve("target/out.json")), "A build output is not a part of it");
    }

    @Test
    void testNothingChangedWhenNothingWasTouched() {
        assertTrue(DiskUtil.changed(DiskUtil.snapshot(project), DiskUtil.snapshot(project)).isEmpty());
    }

    @Test
    void testFileThatWasChangedOrAddedIsFound() throws IOException {
        final var before = DiskUtil.snapshot(project);

        final var scene = project.resolve("scenes/a.json");
        assertTrue(scene.toFile().setLastModified(scene.toFile().lastModified() + 5000));
        Files.writeString(project.resolve("new.json"), "{}");

        assertEquals(List.of(project.resolve("new.json"), scene), DiskUtil.changed(before, DiskUtil.snapshot(project)));
    }

    @Test
    void testFileThatWasDeletedIsNotAChange() throws IOException {
        final var before = DiskUtil.snapshot(project);
        Files.delete(project.resolve("readme.txt"));

        assertTrue(DiskUtil.changed(before, DiskUtil.snapshot(project)).isEmpty());
    }
}
