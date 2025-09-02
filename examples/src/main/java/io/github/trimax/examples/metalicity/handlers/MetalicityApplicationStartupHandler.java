package io.github.trimax.examples.metalicity.handlers;

import io.github.trimax.examples.metalicity.state.MetalicityApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class MetalicityApplicationStartupHandler implements VentaEngineStartupHandler {
    private final MetalicityApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(1f, 1f, 1f));
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));

        final var cube = context.getObjectManager().create("cube", context.getObjectRepository().get("reflective-cube.json"));
        cube.setPosition(new Vector3f(0.0f, 0.0f, 0.0f));
        scene.add(cube);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(3.f, 3.f, 3.f));
        camera.lookAt(new Vector3f(0.f));
        state.setCamera(camera);
    }
}
