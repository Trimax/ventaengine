package io.github.trimax.venta.container.exceptions;

public abstract class AbstractVentaException extends RuntimeException {
    public AbstractVentaException(final String message) {
        super(message);
    }

    public AbstractVentaException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
