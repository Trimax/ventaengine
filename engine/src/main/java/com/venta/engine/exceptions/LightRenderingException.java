package com.venta.engine.exceptions;

public final class LightRenderingException extends VentaException {
    public LightRenderingException(final String message) {
        super(String.format("Can't render a light: %s", message));
    }
}
