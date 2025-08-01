package io.github.trimax.venta.mocks.set4;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentB {
    private ComponentA componentA;
}
