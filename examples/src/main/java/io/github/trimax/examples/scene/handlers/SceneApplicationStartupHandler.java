package io.github.trimax.examples.scene.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@AllArgsConstructor
public final class SceneApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        context.getSceneManager().setCurrent(context.getSceneManager().load("demo.json"));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 4.f, 15.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
