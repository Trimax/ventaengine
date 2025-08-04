package io.github.trimax.examples.light.ambient.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.joml.Vector4f;

@AllArgsConstructor
public final class AmbientLightApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector4f(0.8f, 0.8f, 0.8f, 1.f));

        final var plane = context.getObjectManager().load("plane");
        scene.add(plane);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(1.f, 1.f, 1.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
