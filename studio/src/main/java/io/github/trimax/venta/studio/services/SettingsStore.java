package io.github.trimax.venta.studio.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.trimax.venta.studio.model.settings.StudioSettings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Keeps the settings of the studio in a file of the home folder of the user. A file that is missing or broken gives the default
 * settings: the studio must start whatever happened to it.
 */
@Slf4j
public final class SettingsStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    public SettingsStore(@NonNull final Path file) {
        this.file = file;
    }

    /**
     * The file of the settings of the user: {@code ~/.venta-studio/settings.json}.
     */
    public static Path defaultFile() {
        return Path.of(System.getProperty("user.home"), ".venta-studio", "settings.json");
    }

    public StudioSettings load() {
        if (!Files.isRegularFile(file))
            return StudioSettings.DEFAULT;

        try {
            final var settings = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), StudioSettings.class);
            return settings == null ? StudioSettings.DEFAULT : settings;
        } catch (final IOException | RuntimeException exception) {
            log.warn("The settings {} can't be read, the default ones are used: {}", file, exception.getMessage());
            return StudioSettings.DEFAULT;
        }
    }

    public void save(@NonNull final StudioSettings settings) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(settings), StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            log.warn("The settings {} can't be saved: {}", file, exception.getMessage());
        }
    }
}
