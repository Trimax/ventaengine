package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class FramebufferCreationException extends AbstractVentaException {
    public FramebufferCreationException(final String message) {
        super(String.format("Can't create framebuffer: %s", message));
    }
}
