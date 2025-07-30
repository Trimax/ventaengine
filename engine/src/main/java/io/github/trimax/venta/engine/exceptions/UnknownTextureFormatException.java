package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class UnknownTextureFormatException extends AbstractVentaException {
    public UnknownTextureFormatException(final String message) {
        super(String.format("Can't load a texture: %s", message));
    }
}
