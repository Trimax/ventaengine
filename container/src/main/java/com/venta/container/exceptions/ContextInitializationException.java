package com.venta.container.exceptions;

public final class ContextInitializationException extends RuntimeException {
    public ContextInitializationException(final String message) {
        super(String.format("Can't initialize context: %s", message));
    }
}
