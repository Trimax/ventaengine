package io.github.trimax.examples.scene;

import io.github.trimax.examples.scene.handlers.SceneApplicationStartupHandler;
import io.github.trimax.examples.scene.handlers.SceneApplicationUpdateHandler;
import io.github.trimax.examples.scene.state.SceneApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

/**
 * Castle by the lake: terrain with biomes, water, a model, fog, a lit smoke, a campfire with a flickering light and 3D sound.
 */
public final class SceneVentaApplication implements VentaEngineApplication {
    private final SceneApplicationState state = new SceneApplicationState();

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
        return new SceneApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new SceneApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SceneVentaApplication());
    }
}
