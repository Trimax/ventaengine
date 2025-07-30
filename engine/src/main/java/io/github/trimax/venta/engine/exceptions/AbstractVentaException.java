package io.github.trimax.venta.engine.exceptions;

abstract class AbstractVentaException extends RuntimeException {
    public AbstractVentaException(final String message) {
        super(message);
    }

    public AbstractVentaException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
