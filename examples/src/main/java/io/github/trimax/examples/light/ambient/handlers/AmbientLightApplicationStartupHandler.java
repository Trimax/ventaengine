package io.github.trimax.examples.light.ambient.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class AmbientLightApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.8f, 0.8f, 0.8f));

        final var plane = context.getObjectManager().create("plane", context.getObjectRepository().get("plane.json"));
        scene.add(plane);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(1.f, 1.f, 1.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
