package io.github.trimax.examples.billboard.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
public final class BillboardDemoApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.3f));

        final var billboard = context.getBillboardManager().create("Billboard", context.getBillboardRepository().get("default.json"));
        scene.add(billboard);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
