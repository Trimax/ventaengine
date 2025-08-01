package com.venta.container;

import com.venta.container.cyclic.ComponentA;
import com.venta.container.general.ComponentC;
import com.venta.container.injection.NoInjectConstructorComponent;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.exceptions.ContextInitializationException;
import io.github.trimax.venta.container.exceptions.CyclicDependencyException;
import io.github.trimax.venta.container.exceptions.InjectionConstructorNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VentaApplicationTest {

    @Test
    void shouldThrowContextInitializationExceptionWhenNoInjectConstructor() {
        Exception exception = assertThrows(
                ContextInitializationException.class,
                () -> VentaApplication.run(new String[]{}, NoInjectConstructorComponent.class)
        );

        assertInstanceOf(InjectionConstructorNotFoundException.class, exception.getCause());
        assertInstanceOf(ContextInitializationException.class, exception);
    }

    @Test
    void shouldThrowCyclicDependencyExceptionForCircularDependencies() {
        Exception exception = assertThrows(
                ContextInitializationException.class,
                () -> VentaApplication.run(new String[]{}, ComponentA.class)
        );

        assertInstanceOf(CyclicDependencyException.class, exception.getCause());
    }

    @Test
    public void shouldComponentDBeInjectedIntoComponentC() {
        VentaApplication.run(new String[] {}, ComponentC.class);
    }
}