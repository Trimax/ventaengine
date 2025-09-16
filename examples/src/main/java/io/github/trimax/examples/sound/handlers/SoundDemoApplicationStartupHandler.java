package io.github.trimax.examples.sound.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SoundDemoApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();

        final var sound = context.getSoundSourceManager().create("water", context.getSoundSourceRepository().get("default.json"));
        sound.setPosition(new Vector3f(0.f, 0.f, 0.f));
        sound.setVolume(0.1f);
        sound.setPitch(0.5f);
        sound.play();
        scene.add(sound);

        log.info("Loading sound file: {}", context.getSoundSourceRepository().get("default.json"));
    }
}
