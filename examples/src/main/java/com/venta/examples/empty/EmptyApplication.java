package com.venta.examples.empty;

import com.venta.engine.VentaEngine;
import com.venta.engine.interfaces.VentaEngineApplication;

public final class EmptyApplication {
    public static void main(final String[] args) {
        VentaEngine.run(args, new VentaEngineApplication() {});
    }
}
