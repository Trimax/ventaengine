package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;
import lombok.NonNull;

public final class UnknownInstanceException extends AbstractVentaException {
    public UnknownInstanceException(@NonNull final Class<?> clazz) {
        super(String.format("Can't use an instance of %s class", clazz.getSimpleName()));
    }
}
