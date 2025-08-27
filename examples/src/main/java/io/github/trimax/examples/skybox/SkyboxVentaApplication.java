package io.github.trimax.examples.skybox;

import io.github.trimax.examples.skybox.handlers.SkyboxApplicationInputHandler;
import io.github.trimax.examples.skybox.handlers.SkyboxApplicationStartupHandler;
import io.github.trimax.examples.skybox.state.SkyboxApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class SkyboxVentaApplication implements VentaEngineApplication {
    private final SkyboxApplicationState skyboxApplicationState = new SkyboxApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, true, false, true, AntialiasingSamples.X2);
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new SkyboxApplicationStartupHandler(skyboxApplicationState);
    }

    @Override
    public @NonNull VentaEngineInputHandler getInputHandler() {
        return new SkyboxApplicationInputHandler(skyboxApplicationState);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SkyboxVentaApplication());
    }
}
