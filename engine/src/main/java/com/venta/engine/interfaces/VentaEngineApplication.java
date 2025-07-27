package com.venta.engine.interfaces;

import com.venta.engine.core.Context;
import lombok.NonNull;

public interface VentaEngineApplication {
    default @NonNull VentaEngineConfiguration getConfiguration() {
        return VentaEngineConfiguration.DEFAULT;
    }

    default @NonNull VentaEngineInputHandler getInputHandler() {
        return VentaEngineInputHandler.DEFAULT;
    }

    void onStartup(final String[] args, final Context context);

    void onUpdate(final double delta, final Context context);
}
