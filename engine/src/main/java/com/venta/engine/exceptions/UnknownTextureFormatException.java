package com.venta.engine.exceptions;

public final class UnknownTextureFormatException extends AbstractVentaException {
    public UnknownTextureFormatException(final String message) {
        super(String.format("Can't load a texture: %s", message));
    }
}
