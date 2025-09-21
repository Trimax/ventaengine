package io.github.trimax.examples.sound.handlers;

import io.github.trimax.examples.sound.state.SoundApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@RequiredArgsConstructor
public final class SoundApplicationStartupHandler implements VentaEngineStartupHandler {
    private final SoundApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.8f, 0.8f, 0.8f));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 0.f, 0.f));
        camera.lookAt(new Vector3f(1.f, 0.f, 0.f));

        final var engine = context.getSoundSourceManager().create("Engine", context.getSoundSourceRepository().get("default.json"));
        engine.setPosition(new Vector3f(2.0f, 0.0f, 0.0f));
        engine.setVolume(1.0f);
        engine.setPitch(1.0f);
        engine.play();
        scene.add(engine);

        state.setEngine(engine);
    }
}
