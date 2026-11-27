package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class RenameUtilTest {
    private static final String SCENE = """
            {
              "objects": [
                {"name": "chest.json", "object": "chest.json", "position": {"x": 1, "y": 2, "z": 3}}
              ],
              "lights": [{"name": "Lamp", "light": "chest.json"}]
            }
            """;
    private static final String OBJECT = "{\"program\": \"default.json\", \"root\": {\"name\": \"c\", \"value\": {\"mesh\": \"chest.obj\", \"material\": \"chest.json\"}}}";
    private static final String MATERIAL = "{\"roughness\": 0.3, \"textures\": {\"diffuse\": \"chest/diffuse.png\"}}";

    @TempDir
    private Path project;

    @BeforeEach
    void createProject() throws IOException {
        Files.createDirectories(project.resolve("scenes"));
        Files.createDirectories(project.resolve("objects"));
        Files.createDirectories(project.resolve("lights"));
        Files.createDirectories(project.resolve("materials"));
        Files.createDirectories(project.resolve("textures/chest"));
        Files.createDirectories(project.resolve("meshes"));

        Files.writeString(project.resolve("scenes/a.json"), SCENE);
        Files.writeString(project.resolve("objects/chest.json"), OBJECT);
        Files.writeString(project.resolve("lights/chest.json"), "{\"intensity\": 1}");
        Files.writeString(project.resolve("materials/chest.json"), MATERIAL);
        Files.writeString(project.resolve("textures/chest/diffuse.png"), "png");
        Files.writeString(project.resolve("meshes/chest.obj"), "o chest");
    }

    @Test
    void testRenamingAnObjectChangesTheScenesThatNameItAndNothingElse() throws IOException {
        final var plan = RenameUtil.plan(project, project.resolve("objects/chest.json"), "box.json");

        assertEquals(1, plan.references());
        assertEquals(1, plan.changes().size());
        assertEquals(project.resolve("scenes/a.json"), plan.changes().getFirst().file());

        RenameUtil.apply(plan);

        assertEquals(SCENE.replace("\"object\": \"chest.json\"", "\"object\": \"box.json\""), Files.readString(project.resolve("scenes/a.json")));
        assertTrue(Files.exists(project.resolve("objects/box.json")));
        assertFalse(Files.exists(project.resolve("objects/chest.json")));
        assertEquals(OBJECT, Files.readString(project.resolve("objects/box.json")), "The file itself is moved as it is");
        assertTrue(Files.exists(project.resolve("lights/chest.json")), "A light of the same name is another file");
    }

    @Test
    void testRenamingAMaterialChangesTheObjectsThatNameItNotTheOnesOfTheSameName() throws IOException {
        final var plan = RenameUtil.plan(project, project.resolve("materials/chest.json"), "wood.json");

        assertEquals(1, plan.changes().size());
        assertEquals(project.resolve("objects/chest.json"), plan.changes().getFirst().file());

        RenameUtil.apply(plan);

        assertEquals(OBJECT.replace("\"material\": \"chest.json\"", "\"material\": \"wood.json\""), Files.readString(project.resolve("objects/chest.json")));
        assertEquals(SCENE, Files.readString(project.resolve("scenes/a.json")), "The scene names an object and a light of that name, not a material");
    }

    @Test
    void testRenamingATextureInAFolderKeepsTheFolderInTheName() throws IOException {
        final var plan = RenameUtil.plan(project, project.resolve("textures/chest/diffuse.png"), "color.png");

        RenameUtil.apply(plan);

        assertEquals(MATERIAL.replace("chest/diffuse.png", "chest/color.png"), Files.readString(project.resolve("materials/chest.json")));
        assertTrue(Files.exists(project.resolve("textures/chest/color.png")));
    }

    @Test
    void testFileThatNothingNamesIsJustRenamed() throws IOException {
        Files.writeString(project.resolve("meshes/spare.obj"), "o spare");

        final var plan = RenameUtil.plan(project, project.resolve("meshes/spare.obj"), "box.obj");

        assertEquals(0, plan.references());
        assertTrue(plan.changes().isEmpty());

        RenameUtil.apply(plan);
        assertTrue(Files.exists(project.resolve("meshes/box.obj")));
        assertFalse(Files.exists(project.resolve("meshes/spare.obj")));
    }

    @Test
    void testPlanChangesNothingUntilItIsApplied() throws IOException {
        RenameUtil.plan(project, project.resolve("objects/chest.json"), "box.json");

        assertEquals(SCENE, Files.readString(project.resolve("scenes/a.json")));
        assertTrue(Files.exists(project.resolve("objects/chest.json")));
    }

    @Test
    void testNamesThatAreNotNamesOfFilesAreRefused() {
        final var file = project.resolve("objects/chest.json");

        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, file, ""));
        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, file, "  "));
        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, file, "a/b.json"));
        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, file, "..\\x.json"));
        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, file, ".."));
    }

    @Test
    void testNameOfAnotherFileOfTheFolderOrTheSameNameIsRefused() throws IOException {
        Files.writeString(project.resolve("objects/palm.json"), "{}");

        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, project.resolve("objects/chest.json"), "palm.json"));
        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, project.resolve("objects/chest.json"), "chest.json"));
    }

    @Test
    void testFileOutsideOfTheProjectOrNotAFileIsRefused(@TempDir final Path other) throws IOException {
        Files.writeString(other.resolve("x.json"), "{}");

        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, other.resolve("x.json"), "y.json"));
        assertThrows(IllegalArgumentException.class, () -> RenameUtil.plan(project, project.resolve("objects"), "things"));
    }

    @Test
    void testFilesOfBuildFoldersAndHiddenFoldersAreNotChanged() throws IOException {
        Files.createDirectories(project.resolve("target/scenes"));
        Files.createDirectories(project.resolve(".cache"));
        Files.writeString(project.resolve("target/scenes/a.json"), SCENE);
        Files.writeString(project.resolve(".cache/b.json"), SCENE);

        final var plan = RenameUtil.plan(project, project.resolve("objects/chest.json"), "box.json");

        assertEquals(1, plan.changes().size());
    }

    @Test
    void testRenamingAFileThatNamesItselfKeepsTheNewTextInTheNewFile() throws IOException {
        Files.writeString(project.resolve("materials/loop.json"), "{\"material\": \"loop.json\"}");

        RenameUtil.apply(RenameUtil.plan(project, project.resolve("materials/loop.json"), "ring.json"));

        assertEquals("{\"material\": \"ring.json\"}", Files.readString(project.resolve("materials/ring.json")));
    }

    @Test
    void testFailureLeavesTheFilesAsTheyWere() throws IOException {
        final var plan = RenameUtil.plan(project, project.resolve("objects/chest.json"), "box.json");

        // The place the file is moved to is taken after the plan was made
        Files.writeString(project.resolve("objects/box.json"), "{}");
        Files.delete(project.resolve("objects/box.json"));
        Files.createDirectories(project.resolve("objects/box.json/inside"));

        assertThrows(IOException.class, () -> RenameUtil.apply(plan));

        assertEquals(SCENE, Files.readString(project.resolve("scenes/a.json")), "The scene is as it was");
        assertEquals(OBJECT, Files.readString(project.resolve("objects/chest.json")));
    }
}
