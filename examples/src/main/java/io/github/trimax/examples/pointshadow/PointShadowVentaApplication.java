package io.github.trimax.examples.pointshadow;

import io.github.trimax.examples.pointshadow.handlers.PointShadowApplicationStartupHandler;
import io.github.trimax.examples.pointshadow.handlers.PointShadowApplicationUpdateHandler;
import io.github.trimax.examples.pointshadow.state.PointShadowApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

/**
 * The shadows of point lights: pillars and cubes on a plane in the dark, lit by lights that fly around them and by one that
 * stands still. Every light that casts shadows has a map of its own (up to four of the nearest ones have it), so the pillars
 * throw shadows that turn with the lights.
 */
public final class PointShadowVentaApplication implements VentaEngineApplication {
    private final PointShadowApplicationState state = new PointShadowApplicationState();

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
        return new PointShadowApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new PointShadowApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new PointShadowVentaApplication());
    }
}
