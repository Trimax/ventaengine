package io.github.trimax.venta.container.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.exceptions.CyclicDependencyException;
import io.github.trimax.venta.mocks.set2.MultipleComponentsTestApplication;
import io.github.trimax.venta.mocks.set4.CyclicDependencyTestApplication;
import io.github.trimax.venta.mocks.set8.Events;
import io.github.trimax.venta.mocks.set8.OrderTestApplication;

final class ContainerLifecycleTest {
    @BeforeEach
    void resetEvents() {
        Events.reset();
    }

    @Test
    void testListsAreInjectedInClassNameOrder() {
        VentaApplication.run(new String[] {}, OrderTestApplication.class);

        assertEquals(List.of("ComponentApple", "ComponentMango", "ComponentZebra"), Events.PLUGINS, "List must be sorted by class name");
    }

    @Test
    void testInjectionOrderIsStableBetweenRuns() {
        VentaApplication.run(new String[] {}, OrderTestApplication.class);
        final var first = List.copyOf(Events.PLUGINS);
        Events.reset();

        VentaApplication.run(new String[] {}, OrderTestApplication.class);

        assertEquals(first, Events.PLUGINS, "Every run must inject the same order");
    }

    @Test
    void testDependentsAreClosedBeforeTheirDependencies() {
        VentaApplication.run(new String[] {}, OrderTestApplication.class);

        assertEquals(List.of("UserComponent", "ResourceComponent"), Events.CLOSED, "Components must be closed in reverse creation order");
    }

    @Test
    void testFailedRunDoesNotAffectTheNextOne() {
        assertThrows(CyclicDependencyException.class, () -> VentaApplication.run(new String[] {}, CyclicDependencyTestApplication.class),
                "Cyclic dependency must be reported");

        assertDoesNotThrow(() -> VentaApplication.run(new String[] {}, MultipleComponentsTestApplication.class),
                "Components of a failed run must not be created again by the next run");
    }

    @Test
    void testComponentsOfPreviousRunAreNotKept() {
        VentaApplication.run(new String[] {}, OrderTestApplication.class);

        assertNull(VentaApplication.getComponent(OrderTestApplication.class), "Components must be released after the run");
    }
}
