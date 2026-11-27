package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

final class ImportUtilTest {
    private static final Path PROJECT = Path.of("/project");

    @Test
    void testChosenFolderOfTheProjectIsWhereTheFileGoes() {
        assertEquals(PROJECT.resolve("textures/walls"), ImportUtil.folderFor(PROJECT, PROJECT.resolve("textures/walls"), "x.ogg"));
    }

    @Test
    void testWholeProjectSendsTheFileToTheFolderOfItsKind() {
        assertEquals(PROJECT.resolve("textures"), ImportUtil.folderFor(PROJECT, PROJECT, "wall.PNG"));
        assertEquals(PROJECT.resolve("meshes"), ImportUtil.folderFor(PROJECT, null, "chest.obj"));
        assertEquals(PROJECT.resolve("sounds"), ImportUtil.folderFor(PROJECT, PROJECT, "fire.ogg"));
    }

    @Test
    void testFileOfAnUnknownKindStaysInTheProject() {
        assertEquals(PROJECT, ImportUtil.folderFor(PROJECT, PROJECT, "notes.txt"));
        assertEquals(PROJECT, ImportUtil.folderFor(PROJECT, PROJECT, "noextension"));
    }

    @Test
    void testFreeNameIsKept() {
        assertEquals("wall.png", ImportUtil.uniqueName(PROJECT, "wall.png", path -> false));
    }

    @Test
    void testTakenNameGetsTheNextNumberBeforeTheExtension() {
        final var taken = Set.of(PROJECT.resolve("wall.png"), PROJECT.resolve("wall 2.png"));

        assertEquals("wall 3.png", ImportUtil.uniqueName(PROJECT, "wall.png", taken::contains));
    }

    @Test
    void testNameWithoutAnExtensionOrWithOnlyADotIsNumberedToo() {
        assertEquals("README 2", ImportUtil.uniqueName(PROJECT, "README", path -> path.equals(PROJECT.resolve("README"))));
        assertEquals(".hidden 2", ImportUtil.uniqueName(PROJECT, ".hidden", path -> path.equals(PROJECT.resolve(".hidden"))));
    }
}
