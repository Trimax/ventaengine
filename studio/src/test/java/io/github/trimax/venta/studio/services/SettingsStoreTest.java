package io.github.trimax.venta.studio.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.studio.model.settings.StudioSettings;

final class SettingsStoreTest {
    @Test
    void testMissingFileGivesTheDefaultSettings(@TempDir final Path directory) {
        assertEquals(StudioSettings.DEFAULT, new SettingsStore(directory.resolve("settings.json")).load());
    }

    @Test
    void testSettingsAreSavedAndLoaded(@TempDir final Path directory) {
        final var store = new SettingsStore(directory.resolve("deeper/settings.json"));
        final var settings = StudioSettings.DEFAULT.withProject("/a").withProject("/b").withWindow(1280, 800);

        store.save(settings);

        assertEquals(settings, store.load());
        assertEquals("/b", store.load().lastProject());
    }

    @Test
    void testBrokenFileGivesTheDefaultSettings(@TempDir final Path directory) throws IOException {
        final var file = directory.resolve("settings.json");
        Files.writeString(file, "{ this is not json");

        assertEquals(StudioSettings.DEFAULT, new SettingsStore(file).load());
    }

    @Test
    void testEmptyFileGivesTheDefaultSettings(@TempDir final Path directory) throws IOException {
        final var file = directory.resolve("settings.json");
        Files.writeString(file, "");

        assertEquals(StudioSettings.DEFAULT, new SettingsStore(file).load());
    }

    @Test
    void testFileWithoutTheListGivesNoRecentProjects(@TempDir final Path directory) throws IOException {
        final var file = directory.resolve("settings.json");
        Files.writeString(file, "{\"windowWidth\": 900}");

        final var settings = new SettingsStore(file).load();

        assertEquals(List.of(), settings.recentProjects());
        assertEquals(900, settings.windowWidth());
        assertNull(settings.lastProject());
    }

    @Test
    void testSettingsThatCanNotBeSavedDoNotStopTheStudio(@TempDir final Path directory) throws IOException {
        final var blocker = Files.writeString(directory.resolve("file"), "x");

        assertDoesNotThrow(() -> new SettingsStore(blocker.resolve("settings.json")).save(StudioSettings.DEFAULT));
    }
}
