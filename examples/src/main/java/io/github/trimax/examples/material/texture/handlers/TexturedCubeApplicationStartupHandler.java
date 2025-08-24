package io.github.trimax.examples.material.texture.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class TexturedCubeApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        //TODO: Remove this and create a separate example with archive
        context.registerArchive("c:\\Data\\test.vea");

        final var scene = context.getSceneManager().getCurrent();

        final var cube = context.getObjectManager().create("cube", context.getObjectRepository().get("cube.json"));
        cube.setScale(new Vector3f(5.f));
        scene.add(cube);

        final var lightXZ = context.getLightManager().create("XZ light", context.getLightRepository().get("point.json"));
        lightXZ.setPosition(new Vector3f(3.f, 3.f, 3.f));
        lightXZ.setIntensity(6.0f);
        scene.add(lightXZ);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
