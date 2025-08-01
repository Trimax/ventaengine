package io.github.trimax.venta.mocks.set3;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.annotations.Inject;
import io.github.trimax.venta.mocks.set1.ComponentA;
import io.github.trimax.venta.mocks.set1.ComponentB;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(onConstructor_ = @__(@Inject))
public final class ComponentWithInjectionConstructor {
    private final ComponentA componentA;
    private final ComponentB componentB;
}
