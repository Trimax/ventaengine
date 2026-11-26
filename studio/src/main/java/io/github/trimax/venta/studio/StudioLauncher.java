package io.github.trimax.venta.studio;

import java.nio.file.Path;

import io.github.trimax.venta.container.VentaApplication;

/**
 * Starts the studio. It is not the application itself: JavaFX refuses a main class that extends its application when it is
 * not on the module path (a jar with everything in it, a run configuration of an IDE).
 */
public final class StudioLauncher {
    /**
     * The argument that names the folder of the project to open.
     */
    public static final String PROJECT_ARGUMENT = "--project";

    private StudioLauncher() {
    }

    /**
     * The folder named by the argument {@link #PROJECT_ARGUMENT}, null when there is none.
     */
    public static Path projectOf(final String[] arguments) {
        for (int index = 0; index + 1 < arguments.length; index++)
            if (PROJECT_ARGUMENT.equals(arguments[index]))
                return Path.of(arguments[index + 1]);

        return null;
    }

    public static void main(final String[] args) {
        VentaApplication.runScanning(args, StudioApplication.class, "io.github.trimax.venta.engine");
    }
}
