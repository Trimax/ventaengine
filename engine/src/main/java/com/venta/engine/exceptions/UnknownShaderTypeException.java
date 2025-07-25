package com.venta.engine.exceptions;

public final class UnknownShaderTypeException extends AbstractVentaException {
    public UnknownShaderTypeException(final String message) {
        super(String.format("Unknown shader type: %s", message));
    }
}
