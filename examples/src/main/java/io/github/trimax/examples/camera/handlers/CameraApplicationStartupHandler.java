package io.github.trimax.examples.camera.handlers;

import io.github.trimax.examples.camera.state.CameraApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Slf4j
@AllArgsConstructor
public final class CameraApplicationStartupHandler implements VentaEngineStartupHandler {
    private final CameraApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Camera demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector4f(0.6f, 0.6f, 0.6f, 1.f));

        final var castle = context.getObjectManager().create("Castle", context.getObjectRepository().get("castle.json"));
        castle.move(new Vector3f(0, 1.5f, 0));
        castle.setScale(new Vector3f(3.f));
        scene.add(castle);

        final var camera = context.getCameraManager().getCurrent();
        state.setCamera(camera);
    }
}
