package io.github.trimax.examples.transparency.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class TransparencyApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));
        scene.setAmbientLight(new Vector3f(0.8f, 0.8f, 0.8f));

        final var plane = context.getObjectManager().create("plane", context.getObjectRepository().get("plane.json"));
        plane.setMaterial(context.getMaterialRegistry().get("hole.json"));
        scene.add(plane);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 1.f, 0.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
