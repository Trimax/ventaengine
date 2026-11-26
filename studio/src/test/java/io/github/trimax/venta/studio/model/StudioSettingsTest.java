package io.github.trimax.venta.studio.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.studio.model.settings.StudioSettings;

final class StudioSettingsTest {
    @Test
    void testOpenedProjectIsTheFirstOnce() {
        final var settings = StudioSettings.DEFAULT.withProject("/a").withProject("/b").withProject("/a");

        assertEquals(List.of("/a", "/b"), settings.recentProjects());
        assertEquals("/a", settings.lastProject());
    }

    @Test
    void testOnlyTheLastProjectsAreKept() {
        var settings = StudioSettings.DEFAULT;
        for (int index = 0; index < StudioSettings.MAXIMAL_RECENT_PROJECTS + 5; index++)
            settings = settings.withProject("/project" + index);

        assertEquals(StudioSettings.MAXIMAL_RECENT_PROJECTS, settings.recentProjects().size());
        assertEquals("/project14", settings.lastProject());
    }

    @Test
    void testProjectCanBeForgotten() {
        final var settings = StudioSettings.DEFAULT.withProject("/a").withProject("/b").withoutProject("/b");

        assertEquals(List.of("/a"), settings.recentProjects());
        assertEquals(settings, settings.withoutProject("/missing"));
    }

    @Test
    void testWindowSizeIsKeptWithTheProjects() {
        final var settings = StudioSettings.DEFAULT.withProject("/a").withWindow(1000, 700).withProject("/b");

        assertEquals(1000, settings.windowWidth());
        assertEquals(700, settings.windowHeight());
    }

    @Test
    void testNoProjectsIsAnEmptyList() {
        assertEquals(List.of(), new StudioSettings(null, 0, 0).recentProjects());
        assertNull(StudioSettings.DEFAULT.lastProject());
    }
}
