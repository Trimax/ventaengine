package io.github.trimax.examples.archive.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.file.Paths;

@AllArgsConstructor
public final class ArchiveApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector4f(0.f, 0.f, 0.3f, 0.5f));
        context.registerArchive(Paths.get("examples", "data", "example.vea").toString());

        final var cube = context.getObjectManager().create("cube", context.getObjectRepository().get("cube-without-material.json"));
        cube.setScale(new Vector3f(5.f));
        scene.add(cube);

        final var lightXZ = context.getLightManager().create("Y light", context.getLightRepository().get("point.json"));
        lightXZ.setPosition(new Vector3f(0.f, 5.f, 0.f));
        lightXZ.setIntensity(1.0f);
        scene.add(lightXZ);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
