package io.github.trimax.examples.spotlight;

import io.github.trimax.examples.spotlight.handlers.SpotLightApplicationStartupHandler;
import io.github.trimax.examples.spotlight.handlers.SpotLightApplicationUpdateHandler;
import io.github.trimax.examples.spotlight.state.SpotLightApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

/**
 * Spot lights: pillars and cubes on a plane in the dark, lit by cones of light (a still one and two that sweep over the
 * plane). A cone is full inside its inner angle and fades to nothing at its outer one, and the pillars throw shadows in it.
 */
public final class SpotLightVentaApplication implements VentaEngineApplication {
    private final SpotLightApplicationState state = new SpotLightApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(true, false, true, true, AntialiasingSamples.X8);
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new SpotLightApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new SpotLightApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SpotLightVentaApplication());
    }
}
