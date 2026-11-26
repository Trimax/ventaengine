package io.github.trimax.venta.studio;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class StudioLauncherTest {
    @Test
    void testProjectIsTheValueOfTheArgument() {
        assertEquals(Path.of("/work/beach"), StudioLauncher.projectOf(new String[]{"--project", "/work/beach"}));
        assertEquals(Path.of("/work/beach"), StudioLauncher.projectOf(new String[]{"other", "--project", "/work/beach", "more"}));
    }

    @Test
    void testNoProjectWithoutTheArgumentOrItsValue() {
        assertNull(StudioLauncher.projectOf(new String[]{}));
        assertNull(StudioLauncher.projectOf(new String[]{"--project"}));
        assertNull(StudioLauncher.projectOf(new String[]{"/work/beach"}));
    }
}
