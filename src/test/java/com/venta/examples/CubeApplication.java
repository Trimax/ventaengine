package com.venta.examples;

import com.venta.engine.VentaEngine;

public final class CubeApplication {
    public static void main(String[] args) {
        VentaEngine.run(args, new RotatingCube());
    }
}
