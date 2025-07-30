package io.github.trimax.venta.container.exceptions;

import io.github.trimax.venta.container.annotations.Inject;

public final class InjectionConstructorNotFoundException extends AbstractVentaException {
    public InjectionConstructorNotFoundException(final Class<?> clazz) {
        super(String.format("Cannot determine which constructor to use for %s. %s", clazz.getSimpleName(),
                ". Use @" + Inject.class.getSimpleName() + " to mark the constructor explicitly."));
    }
}
