package io.github.trimax.examples.fire.handlers;

import io.github.trimax.examples.fire.state.FireApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@RequiredArgsConstructor
public final class FireApplicationStartupHandler implements VentaEngineStartupHandler {
    private final FireApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.3f));
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));

        final var billboard = context.getBillboardManager().create("Billboard", context.getBillboardRepository().get("fire.json"));
        scene.add(billboard);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(1.f));
        camera.lookAt(new Vector3f(0.f));
        state.setCamera(camera);
    }
}
