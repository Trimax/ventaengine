package io.github.trimax.venta.container.exceptions;

import io.github.trimax.venta.container.annotations.Inject;

public final class InjectionConstructorNotFoundException extends AbstractVentaException {
    public InjectionConstructorNotFoundException(final String path) {
        super(String.format("Cannot determine which constructor to use for %s. Use %s to mark the constructor explicitly.",
                path, Inject.class.getSimpleName()));
    }
}
