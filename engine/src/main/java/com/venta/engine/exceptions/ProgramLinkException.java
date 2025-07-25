package com.venta.engine.exceptions;

public final class ProgramLinkException extends AbstractVentaException {
    public ProgramLinkException(final String message) {
        super(String.format("Can't link program: %s", message));
    }
}
