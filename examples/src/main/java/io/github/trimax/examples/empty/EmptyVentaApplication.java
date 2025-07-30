package io.github.trimax.examples.empty;

import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;

public final class EmptyVentaApplication {
    public static void main(final String[] args) {
        VentaEngine.run(args, new VentaEngineApplication() {});
    }
}
