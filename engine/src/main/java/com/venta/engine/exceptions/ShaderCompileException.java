package com.venta.engine.exceptions;

public final class ShaderCompileException extends VentaException {
    public ShaderCompileException(final String message) {
        super(String.format("Can't compile shader: %s", message));
    }
}
