package io.github.trimax.examples.material.texture.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class TexturedChestApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();

        final var chest = context.getObjectManager().create("chest", context.getObjectRepository().get("chest.json"));
        chest.setScale(new Vector3f(5.f));
        scene.add(chest);

        final var lightXZ = context.getLightManager().create("XZ light", context.getLightRepository().get("point.json"));
        lightXZ.setPosition(new Vector3f(3.f, 3.f, 3.f));
        lightXZ.setIntensity(2.0f);
        scene.add(lightXZ);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
