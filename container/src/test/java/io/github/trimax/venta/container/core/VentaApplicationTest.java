package io.github.trimax.venta.container.core;

import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.exceptions.CyclicDependencyException;
import io.github.trimax.venta.container.exceptions.InjectionConstructorNotFoundException;
import io.github.trimax.venta.mocks.set2.MultipleComponentsTestApplication;
import io.github.trimax.venta.mocks.set4.CyclicDependencyTestApplication;
import io.github.trimax.venta.mocks.set5.SingleComponentTestApplication;
import io.github.trimax.venta.mocks.set6.NoInjectConstructorTestApplication;
import io.github.trimax.venta.mocks.set7.InjectConstructorTestApplication;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class VentaApplicationTest {
    @Test
    void testShouldThrowExceptionWhenMultipleConstructorsFound() {
        assertThrows(InjectionConstructorNotFoundException.class, () -> {
            VentaApplication.run(new String[] {}, NoInjectConstructorTestApplication.class);
        }, "VentaApplication should throw an exception when the multiple constructors found");
    }

    @Test
    void testInjectConstructorUsedWhenMultipleDefined() {
        VentaApplication.run(new String[] {}, InjectConstructorTestApplication.class);
    }

    @Test
    void testShouldThrowExceptionWhenCyclicDependencyFound() {
        assertThrows(CyclicDependencyException.class, () -> {
            VentaApplication.run(new String[] {}, CyclicDependencyTestApplication.class);
        }, "VentaApplication should throw an exception when cyclic dependency found");
    }

    @Test
    public void testComponentInjection() {
        VentaApplication.run(new String[] {}, SingleComponentTestApplication.class);
    }

    @Test
    public void testMultipleComponentsInjection() {
        VentaApplication.run(new String[] {}, MultipleComponentsTestApplication.class);
    }
}
