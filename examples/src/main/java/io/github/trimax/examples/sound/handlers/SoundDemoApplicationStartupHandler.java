package io.github.trimax.examples.sound.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SoundDemoApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();

        final var sound = context.getSoundSourceManager().create("engine", context.getSoundSourceRepository().get("default.json"));
        sound.setVolume(1f);
        sound.setPitch(1f);
        sound.play();
        scene.add(sound);
    }
}
