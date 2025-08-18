package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class UnknownFileFormatException extends AbstractVentaException {
    public UnknownFileFormatException(final String extension) {
        super(String.format("Unknown file format: %s", extension));
    }
}
