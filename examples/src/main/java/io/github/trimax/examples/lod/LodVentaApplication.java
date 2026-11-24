package io.github.trimax.examples.lod;

import io.github.trimax.examples.lod.handlers.LodApplicationStartupHandler;
import io.github.trimax.examples.lod.handlers.LodApplicationUpdateHandler;
import io.github.trimax.examples.lod.state.LodApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

/**
 * Levels of detail: hundreds of spheres in rows, every one has four levels (a sphere of 960, 224 and 48 triangles and a
 * card with a picture of it), and the camera flies along the rows. The color of a level tells which one is drawn: green is
 * the main model, yellow and orange are the simpler ones and the cards are green too, but flat.
 */
public final class LodVentaApplication implements VentaEngineApplication {
    private final LodApplicationState state = new LodApplicationState();

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
        return new LodApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new LodApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new LodVentaApplication());
    }
}
