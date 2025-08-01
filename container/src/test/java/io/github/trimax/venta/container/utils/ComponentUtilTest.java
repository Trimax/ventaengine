package io.github.trimax.venta.container.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.mocks.set1.ComponentA;
import io.github.trimax.venta.mocks.set1.ComponentB;
import io.github.trimax.venta.mocks.set3.ComponentWithInjectionConstructor;
import io.github.trimax.venta.mocks.set3.ComponentWithoutInjectionConstructor;

final class ComponentUtilTest {
    @Test
    void testPackageScan() {
        final var components = ComponentUtil.scan(ComponentA.class.getPackageName());

        assertNotNull(components, "Components list should not be null");
        assertEquals(2, components.size(), "Components list must contain two components");
        assertTrue(components.contains(ComponentA.class), ComponentA.class.getSimpleName() + " should be present");
        assertTrue(components.contains(ComponentB.class), ComponentA.class.getSimpleName() + " should be present");
    }

    @Test
    void testInjectConstructor() {
        assertNotNull(ComponentUtil.findInjectConstructor(ComponentWithInjectionConstructor.class), ComponentA.class.getSimpleName() + " must have injection constructor");
        assertNull(ComponentUtil.findInjectConstructor(ComponentWithoutInjectionConstructor.class), ComponentA.class.getSimpleName() + " must not have injection constructor");
    }
}
