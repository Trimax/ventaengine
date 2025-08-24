package io.github.trimax.examples.archive;

import io.github.trimax.examples.archive.handlers.ArchiveApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class ArchiveVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new ArchiveApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new ArchiveVentaApplication());
    }
}
