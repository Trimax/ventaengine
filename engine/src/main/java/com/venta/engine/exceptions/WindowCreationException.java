package com.venta.engine.exceptions;

public final class WindowCreationException extends VentaException {
    public WindowCreationException(final String message) {
        super(String.format("Can't create window: %s", message));
    }
}
