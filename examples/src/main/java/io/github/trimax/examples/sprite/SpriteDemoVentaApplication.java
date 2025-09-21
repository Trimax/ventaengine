package io.github.trimax.examples.sprite;

import io.github.trimax.examples.sprite.handlers.SpriteDemoApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class SpriteDemoVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new SpriteDemoApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SpriteDemoVentaApplication());
    }
}
