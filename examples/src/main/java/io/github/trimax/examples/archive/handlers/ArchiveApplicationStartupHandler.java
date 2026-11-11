package io.github.trimax.examples.archive.handlers;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public final class ArchiveApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final String EXAMPLE_ARCHIVE = "/archives/example.vea";

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.f, 0.f, 0.15f));

        // An archive path can be passed as the first argument, otherwise the bundled example archive is used
        context.registerArchive(args.length > 0 ? args[0] : extractExampleArchive());
        context.getTextureRegistry().get("texture.png");
        context.getTextureRegistry().get("group/texture.png");
    }

    /** Archives are registered by file path, so the bundled archive is copied to a temporary file first */
    @SneakyThrows
    private static String extractExampleArchive() {
        final var archive = Files.createTempFile("venta-example-", ".vea");
        archive.toFile().deleteOnExit();

        try (final var input = ArchiveApplicationStartupHandler.class.getResourceAsStream(EXAMPLE_ARCHIVE)) {
            Files.copy(Objects.requireNonNull(input, "Example archive is not found: " + EXAMPLE_ARCHIVE), archive, StandardCopyOption.REPLACE_EXISTING);
        }

        return archive.toString();
    }
}
