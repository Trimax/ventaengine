package com.venta.engine.exceptions;

public final class EngineInitializationException extends VentaException {
    public EngineInitializationException(final String message) {
        super(String.format("Can't initialize Venta engine: %s", message));
    }
}
