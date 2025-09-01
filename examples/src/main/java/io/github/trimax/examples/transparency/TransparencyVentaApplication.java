package io.github.trimax.examples.transparency;

import io.github.trimax.examples.transparency.handlers.TransparencyApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class TransparencyVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new TransparencyApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new TransparencyVentaApplication());
    }
}
