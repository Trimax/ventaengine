package io.github.trimax.examples.emitter.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class EmitterApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.9f));

        final var emitter = context.getEmitterManager().create("emitter", context.getEmitterRepository().get("smoke.json"));
        scene.add(emitter);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(4.f, 4.f, 4.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
