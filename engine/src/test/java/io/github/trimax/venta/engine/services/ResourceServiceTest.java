package io.github.trimax.venta.engine.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.exceptions.ResourceNotFoundException;
import io.github.trimax.venta.engine.utils.ArchiveTestWriter;

final class ResourceServiceTest {
    /** A resource of the engine's classpath */
    private static final String CLASSPATH_PROGRAM = "/programs/default.json";

    private ResourceService service;

    @BeforeEach
    void createService() throws Exception {
        final var constructor = ResourceService.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        service = constructor.newInstance();
    }

    @Test
    void testClasspathResource() {
        assertTrue(service.getAsString(CLASSPATH_PROGRAM).contains("shaderVertex"), "Classpath resource must be loaded");
    }

    @Test
    void testMissingResource() {
        assertThrows(ResourceNotFoundException.class, () -> service.getAsBytes("/textures/missing.png"), "Missing resource must be reported");
    }

    @Test
    void testArchiveResourceTakesPrecedenceOverClasspath(@TempDir final Path directory) throws IOException {
        final var archive = directory.resolve("test.vea");
        try (final var writer = new ArchiveTestWriter(archive)) {
            writer.group(GroupType.Program, 1).file("default.json", "{\"from\": \"archive\"}");
            writer.group(GroupType.Material, 1).file("only-in-archive.json", "{}");
        }

        service.loadArchive(archive.toString());

        assertEquals("{\"from\": \"archive\"}", service.getAsString(CLASSPATH_PROGRAM), "Archive resource must override the classpath one");
        assertEquals("{}", service.getAsString("/materials/only-in-archive.json"), "Resource present only in the archive must be loaded");
        assertNotNull(service.getAsBytes("/textures/default/diffuse.png"), "Resources missing in the archive must fall back to the classpath");
    }

    @Test
    void testObjectFromArchive(@TempDir final Path directory) throws IOException {
        final var archive = directory.resolve("test.vea");
        try (final var writer = new ArchiveTestWriter(archive)) {
            writer.group(GroupType.Material, 1).file("value.json", "{\"name\": \"stone\", \"size\": 3}");
        }

        service.loadArchive(archive.toString());

        final var value = service.getAsObject("/materials/value.json", Value.class);
        assertEquals(new Value("stone", 3), value, "JSON resource must be parsed");
    }

    private record Value(String name, int size) {}

    @Test
    void testProjectFileTakesPrecedenceOverArchiveAndClasspath(@TempDir final Path directory) throws IOException {
        final var archive = directory.resolve("test.vea");
        try (final var writer = new ArchiveTestWriter(archive)) {
            writer.group(GroupType.Program, 1).file("default.json", "{\"from\": \"archive\"}");
        }
        service.loadArchive(archive.toString());

        final var project = Files.createDirectories(directory.resolve("project/programs"));
        Files.writeString(project.resolve("default.json"), "{\"from\": \"project\"}");
        service.useProject(directory.resolve("project"));

        assertEquals("{\"from\": \"project\"}", service.getAsString(CLASSPATH_PROGRAM));
        assertNotNull(service.getAsBytes("/textures/default/diffuse.png"), "What the project doesn't have comes from the classpath");
    }

    @Test
    void testProjectFileIsReadFromTheDiskEveryTime(@TempDir final Path directory) throws IOException {
        Files.createDirectories(directory.resolve("scenes"));
        final var file = directory.resolve("scenes/beach.json");
        service.useProject(directory);

        Files.writeString(file, "one");
        assertEquals("one", service.getAsString("/scenes/beach.json"));

        Files.writeString(file, "two");
        assertEquals("two", service.getAsString("/scenes/beach.json"), "A changed file is loaded changed");
    }

    @Test
    void testPathThatLeavesTheProjectIsNotAResource(@TempDir final Path directory) throws IOException {
        Files.writeString(directory.resolve("secret.txt"), "secret");
        final var project = Files.createDirectories(directory.resolve("project"));
        service.useProject(project);

        assertThrows(ResourceNotFoundException.class, () -> service.getAsString("/../secret.txt"));
        assertThrows(ResourceNotFoundException.class, () -> service.getAsString("/scenes/../../secret.txt"));
    }

    @Test
    void testFolderIsNotAResource(@TempDir final Path directory) throws IOException {
        Files.createDirectories(directory.resolve("ghosts"));
        service.useProject(directory);

        assertThrows(ResourceNotFoundException.class, () -> service.getAsBytes("/ghosts"));
    }

    @Test
    void testProjectMustBeAFolder(@TempDir final Path directory) throws IOException {
        final var file = Files.writeString(directory.resolve("file.txt"), "x");

        assertThrows(IllegalArgumentException.class, () -> service.useProject(file));
        assertThrows(IllegalArgumentException.class, () -> service.useProject(directory.resolve("missing")));
        assertTrue(service.getProject().isEmpty(), "A refused folder is not the project");
    }

    @Test
    void testProjectIsKnown(@TempDir final Path directory) {
        assertTrue(service.getProject().isEmpty());

        service.useProject(directory);

        assertEquals(directory.toAbsolutePath().normalize(), service.getProject().orElseThrow());
    }
}
