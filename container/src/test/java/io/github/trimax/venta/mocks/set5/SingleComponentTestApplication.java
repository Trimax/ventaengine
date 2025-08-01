package io.github.trimax.venta.mocks.set5;


import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleComponentTestApplication implements AbstractVentaApplication<Void> {
    private TestComponent testComponent;

    @Override
    public void start(final String[] args, final Void argument) {
        assertNotNull(testComponent, "TestComponent should be injected into MultipleComponentsTestApplication");
    }
}
