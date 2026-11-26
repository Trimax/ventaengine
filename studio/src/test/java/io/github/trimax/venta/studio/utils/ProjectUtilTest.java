package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.engine.enums.GroupType;

final class ProjectUtilTest {
    @Test
    void testPathBelongsToTheGroupOfItsFolder(@TempDir final Path project) {
        assertEquals(GroupType.Scene, ProjectUtil.groupOf(project, project.resolve("scenes/beach.json")));
        assertEquals(GroupType.Texture, ProjectUtil.groupOf(project, project.resolve("textures/sand/diffuse.png")));
        assertEquals(GroupType.Material, ProjectUtil.groupOf(project, project.resolve("materials")), "The folder of a group is the group");
    }

    @Test
    void testLongerFolderOfAGroupWins(@TempDir final Path project) {
        assertEquals(GroupType.TerrainSurface, ProjectUtil.groupOf(project, project.resolve("surfaces/terrain/beach.json")));
        assertEquals(GroupType.WaterSurface, ProjectUtil.groupOf(project, project.resolve("surfaces/water/sea.json")));
        assertNull(ProjectUtil.groupOf(project, project.resolve("surfaces/other.json")), "surfaces itself is not a group");
    }

    @Test
    void testPathOutsideOfTheGroupsHasNoGroup(@TempDir final Path project) {
        assertNull(ProjectUtil.groupOf(project, project.resolve("notes/todo.txt")));
        assertNull(ProjectUtil.groupOf(project, project.resolve("scenesque/a.json")), "A folder that starts like a group is not the group");
    }

    @Test
    void testFoldersAndFilesAreListedByNameWithoutWhatIsNotAProject(@TempDir final Path project) throws IOException {
        Files.createDirectories(project.resolve("textures"));
        Files.createDirectories(project.resolve("Scenes"));
        Files.createDirectories(project.resolve(".git"));
        Files.createDirectories(project.resolve("target"));
        Files.writeString(project.resolve("readme.md"), "x");
        Files.writeString(project.resolve(".hidden"), "x");

        assertEquals(List.of("Scenes", "textures"), ProjectUtil.listFolders(project).stream().map(path -> path.getFileName().toString()).toList());
        assertEquals(List.of("readme.md"), ProjectUtil.listFiles(project).stream().map(path -> path.getFileName().toString()).toList());
    }

    @Test
    void testMissingFolderHasNothingInIt(@TempDir final Path project) {
        assertTrue(ProjectUtil.listFiles(project.resolve("missing")).isEmpty());
        assertTrue(ProjectUtil.listFolders(project.resolve("missing")).isEmpty());
    }

    @Test
    void testFilesArePreviewedByTheirExtension() {
        assertEquals(ProjectUtil.Preview.Image, ProjectUtil.previewOf(Path.of("sand.PNG")));
        assertEquals(ProjectUtil.Preview.Text, ProjectUtil.previewOf(Path.of("beach.json")));
        assertEquals(ProjectUtil.Preview.Text, ProjectUtil.previewOf(Path.of("sphere.obj")));
        assertEquals(ProjectUtil.Preview.Binary, ProjectUtil.previewOf(Path.of("surf.ogg")));
        assertEquals(ProjectUtil.Preview.Binary, ProjectUtil.previewOf(Path.of("noextension")));
    }

    @Test
    void testSceneIsNamedByItsPathInsideTheFolderOfTheScenes(@TempDir final Path project) {
        assertEquals("beach.json", ProjectUtil.sceneNameOf(project, project.resolve("scenes/beach.json")));
        assertEquals("levels/one.json", ProjectUtil.sceneNameOf(project, project.resolve("scenes/levels/one.json")));
        assertNull(ProjectUtil.sceneNameOf(project, project.resolve("objects/chest.json")), "It is not in the folder of the scenes");
        assertNull(ProjectUtil.sceneNameOf(project, project.resolve("scenes/readme.md")), "It is not a scene file");
    }
}
