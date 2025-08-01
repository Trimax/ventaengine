package io.github.trimax.venta.mocks.set3;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.mocks.set1.ComponentA;
import io.github.trimax.venta.mocks.set1.ComponentB;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public final class ComponentWithoutInjectionConstructor {
    private final ComponentA componentA;
    private final ComponentB componentB;
}
