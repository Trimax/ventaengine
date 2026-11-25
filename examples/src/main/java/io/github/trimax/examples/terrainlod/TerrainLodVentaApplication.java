package io.github.trimax.examples.terrainlod;

import io.github.trimax.examples.terrainlod.handlers.TerrainLodApplicationStartupHandler;
import io.github.trimax.examples.terrainlod.handlers.TerrainLodApplicationUpdateHandler;
import io.github.trimax.examples.terrainlod.state.TerrainLodApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

/**
 * The levels of detail of a terrain: a huge terrain (4 by 4 kilometers, over a million cells, 1024 chunks of 125 by 125 meters),
 * and a camera that flies over it. The terrain is drawn as lines at first, so the chunks nearer to the camera are seen dense
 * and the farther ones coarser, and a level is swapped for another as the camera goes. The space key switches to the solid
 * terrain, where the levels (and the skirts that close the cracks between them) should not be seen. {@code scene info} in the
 * console tells how many triangles of the terrain were drawn.
 */
public final class TerrainLodVentaApplication implements VentaEngineApplication {
    private final TerrainLodApplicationState state = new TerrainLodApplicationState();

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
        return new TerrainLodApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new TerrainLodApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new TerrainLodVentaApplication());
    }
}
