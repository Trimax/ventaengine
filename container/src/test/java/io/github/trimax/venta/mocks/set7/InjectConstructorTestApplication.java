package io.github.trimax.venta.mocks.set7;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.annotations.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @__(@Inject))
public final class InjectConstructorTestApplication implements AbstractVentaApplication<Void> {
    private TestComponent useless;

    @Override
    public void start(final String[] args, final Void argument) {
    }
}
