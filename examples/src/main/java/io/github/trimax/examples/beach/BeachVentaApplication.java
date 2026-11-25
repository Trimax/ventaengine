package io.github.trimax.examples.beach;

import io.github.trimax.examples.beach.handlers.BeachApplicationStartupHandler;
import io.github.trimax.examples.beach.handlers.BeachApplicationUpdateHandler;
import io.github.trimax.examples.beach.state.BeachApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

/**
 * An ocean beach at sunrise: a wide sandy beach that goes to the horizon, a chest and a palm on the sand that throw long shadows,
 * the sun low over the sea that shines through the haze, waves that roll in and wash up the sand (it is wet and white with foam
 * there), and the sound of the surf. The camera stands on the sand and looks at the sea; it sways a little, like a person who
 * stands and looks. The whole scene is in scenes/beach.json.
 */
public final class BeachVentaApplication implements VentaEngineApplication {
    private final BeachApplicationState state = new BeachApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, false, true, true, AntialiasingSamples.X8);
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new BeachApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new BeachApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new BeachVentaApplication());
    }
}
