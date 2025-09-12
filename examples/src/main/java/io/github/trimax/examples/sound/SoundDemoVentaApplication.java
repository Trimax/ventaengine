package io.github.trimax.examples.sound;

import io.github.trimax.examples.sound.handlers.SoundDemoApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class SoundDemoVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new SoundDemoApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SoundDemoVentaApplication());
    }
}
