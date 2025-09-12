package io.github.trimax.examples.camera.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.camera.state.CameraApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class CameraApplicationStartupHandler implements VentaEngineStartupHandler {
    private final CameraApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Camera demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.6f, 0.6f, 0.6f));

        final var castle = context.getObjectManager().create("Lime", context.getObjectRepository().get("lime.json"));
        castle.move(new Vector3f(0, 1.5f, 0));
        castle.setScale(new Vector3f(3.f));
        scene.add(castle);

        final var camera = context.getCameraManager().getCurrent();
        state.setCamera(camera);
    }
}
