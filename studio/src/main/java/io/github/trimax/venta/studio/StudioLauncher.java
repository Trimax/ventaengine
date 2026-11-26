package io.github.trimax.venta.studio;

import io.github.trimax.venta.container.VentaApplication;

/**
 * Starts the studio. It is not the application itself: JavaFX refuses a main class that extends its application when it is
 * not on the module path (a jar with everything in it, a run configuration of an IDE).
 */
public final class StudioLauncher {
    private StudioLauncher() {
    }

    public static void main(final String[] args) {
        VentaApplication.runScanning(args, StudioApplication.class, "io.github.trimax.venta.engine");
    }
}
