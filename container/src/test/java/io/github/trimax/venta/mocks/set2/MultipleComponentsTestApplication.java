package io.github.trimax.venta.mocks.set2;


import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipleComponentsTestApplication implements AbstractVentaApplication<Void> {
    private final List<AbstractComponent> components;

    @Override
    public void start(final String[] args, final Void argument) {
        assertNotNull(components, "TestComponent should be injected into MultipleComponentsTestApplication");
        assertEquals(2, components.size(), "TestComponent should be injected into MultipleComponentsTestApplication");
    }
}
