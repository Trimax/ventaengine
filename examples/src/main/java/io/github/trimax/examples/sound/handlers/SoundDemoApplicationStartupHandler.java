package io.github.trimax.examples.sound.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.entity.SoundEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SoundDemoApplicationStartupHandler implements VentaEngineStartupHandler {

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Loading sound file: " + context.getSoundRegistry().get("water.ogg"));
    }
}
