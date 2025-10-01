package io.github.trimax.examples.archive.handlers;

import java.nio.file.Paths;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ArchiveApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.f, 0.f, 0.15f));
        context.registerArchive(Paths.get("examples", "data", "example.vea").toString());
        context.getTextureRegistry().get("1m.jpg");
    }
}
