package io.github.trimax.venta.container.exceptions;

public final class ContextInitializationException extends AbstractVentaException {
    public ContextInitializationException(final String message, final Throwable cause) {
        super(String.format("Can't initialize context: %s", message), cause);
    }
}
