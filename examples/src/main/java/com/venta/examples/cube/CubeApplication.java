package com.venta.examples.cube;

import com.venta.engine.VentaEngine;

public final class CubeApplication {
    public static void main(final String[] args) {
        VentaEngine.run(args, new RotatingCube());
    }
}
