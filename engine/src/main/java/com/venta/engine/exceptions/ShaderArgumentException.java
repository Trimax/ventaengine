package com.venta.engine.exceptions;

public final class ShaderArgumentException extends VentaException {
    public ShaderArgumentException(final String message) {
        super(String.format("Shader argument doesn't exist: %s", message));
    }
}
