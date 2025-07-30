package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class ProgramLinkException extends AbstractVentaException {
    public ProgramLinkException(final String message) {
        super(String.format("Can't link program: %s", message));
    }
}
