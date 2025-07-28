package com.venta.engine.interfaces;

import lombok.NonNull;

public interface VentaEngineApplication {
    default @NonNull VentaEngineConfiguration getConfiguration() {
        return VentaEngineConfiguration.DEFAULT;
    }

    default @NonNull VentaEngineStartupHandler getStartupHandler() {
        return VentaEngineStartupHandler.DEFAULT;
    }

    default @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return VentaEngineUpdateHandler.DEFAULT;
    }

    default @NonNull VentaEngineInputHandler getInputHandler() {
        return VentaEngineInputHandler.DEFAULT;
    }
}
