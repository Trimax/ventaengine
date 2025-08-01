package io.github.trimax.venta.mocks.set4;

import io.github.trimax.venta.container.AbstractVentaApplication;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CyclicDependencyTestApplication implements AbstractVentaApplication<Void> {
    @Override
    public void start(final String[] args, final Void argument) {
    }
}
