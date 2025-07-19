package com.venta.engine.exception;

public final class UnknownShaderTypeException extends VentaException {
    public UnknownShaderTypeException(final String message) {
        super(String.format("Unknown shader type: %s", message));
    }
}
