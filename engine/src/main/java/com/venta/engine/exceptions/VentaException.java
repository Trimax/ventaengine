package com.venta.engine.exceptions;

abstract class VentaException extends RuntimeException {
    public VentaException(final String message) {
        super(message);
    }

    public VentaException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
