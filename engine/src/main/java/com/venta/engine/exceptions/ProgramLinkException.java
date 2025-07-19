package com.venta.engine.exceptions;

public final class ProgramLinkException extends VentaException {
    public ProgramLinkException(final String message) {
        super(String.format("Can't link program: %s", message));
    }
}
