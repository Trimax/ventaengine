package io.github.trimax.examples.light.ambient;

import io.github.trimax.examples.light.ambient.handlers.AmbientLightApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class AmbientLightVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new AmbientLightApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new AmbientLightVentaApplication());
    }
}
