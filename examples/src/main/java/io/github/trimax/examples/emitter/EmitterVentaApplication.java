package io.github.trimax.examples.emitter;

import io.github.trimax.examples.emitter.handlers.EmitterApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class EmitterVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new EmitterApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new EmitterVentaApplication());
    }
}
