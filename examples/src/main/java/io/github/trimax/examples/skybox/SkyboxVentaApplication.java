package io.github.trimax.examples.skybox;

import io.github.trimax.examples.skybox.handlers.SkyboxApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class SkyboxVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new SkyboxApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SkyboxVentaApplication());
    }
}
