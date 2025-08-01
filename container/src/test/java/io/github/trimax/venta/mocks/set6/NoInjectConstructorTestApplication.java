package io.github.trimax.venta.mocks.set6;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
@AllArgsConstructor
public final class NoInjectConstructorTestApplication implements AbstractVentaApplication<Void> {
    private String useless;

    @Override
    public void start(final String[] args, final Void argument) {
    }
}
