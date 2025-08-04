package io.github.trimax.examples.material.texture;

import io.github.trimax.examples.material.texture.handlers.TexturedCubeApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class TexturedCubeVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new TexturedCubeApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new TexturedCubeVentaApplication());
    }
}
