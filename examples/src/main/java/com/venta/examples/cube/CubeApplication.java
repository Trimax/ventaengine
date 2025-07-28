package com.venta.examples.cube;

import com.venta.engine.VentaEngine;
import com.venta.engine.core.Context;
import com.venta.engine.interfaces.VentaEngineApplication;
import com.venta.engine.interfaces.VentaEngineConfiguration;
import com.venta.engine.interfaces.VentaEngineInputHandler;
import com.venta.examples.cube.handlers.CubeApplicationInputHandler;
import com.venta.examples.cube.handlers.CubeApplicationStartupHandler;
import com.venta.examples.cube.handlers.CubeApplicationUpdateHandler;
import com.venta.examples.cube.state.CubeApplicationState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CubeApplication implements VentaEngineApplication {
    private final CubeApplicationState state = new CubeApplicationState();

    private final CubeApplicationStartupHandler cubeApplicationStartupHandler = new CubeApplicationStartupHandler(state);
    private final CubeApplicationUpdateHandler cubeApplicationUpdateHandler = new CubeApplicationUpdateHandler(state);

    @Override
    public VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, true);
            }
        };
    }

    @Override
    public VentaEngineInputHandler getInputHandler() {
        return new CubeApplicationInputHandler(state);
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        cubeApplicationStartupHandler.onStartup(args, context);
    }

    @Override
    public void onUpdate(final double delta, final Context context) {
        cubeApplicationUpdateHandler.onUpdate(delta, context);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new CubeApplication());
    }
}
