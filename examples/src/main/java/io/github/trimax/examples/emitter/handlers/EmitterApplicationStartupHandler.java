package io.github.trimax.examples.emitter.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class EmitterApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));
        scene.setAmbientLight(new Vector3f(0.9f));

        final var emitterPrefab = context.getEmitterRepository().get("smoke.json");
        for (int emitterID = 0; emitterID < 10; emitterID++) {
            final var angle = emitterID * (2.f * Math.PI / 10.f);

            final var emitter = context.getEmitterManager().create("emitter" + emitterID, emitterPrefab);
            emitter.setPosition(new Vector3f((float) Math.sin(angle), 0.f, (float) Math.cos(angle)).mul(2.5f));
            scene.add(emitter);
        }

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(4.f, 4.f, 4.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
