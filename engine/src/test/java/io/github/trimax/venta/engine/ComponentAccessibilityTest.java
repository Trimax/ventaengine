package io.github.trimax.venta.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.utils.ComponentUtil;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

public final class ComponentAccessibilityTest {
    @Test
    public void testAllComponentsHavePrivateConstructors() {
        StreamEx.of(getAllVentaComponents()).forEach(this::verifyComponent);
    }

    private void verifyComponent(final Class<?> component) {
        if (Modifier.isAbstract(component.getModifiers()))
            return;

        StreamEx.of(component.getDeclaredConstructors()).forEach(this::verifyConstructor);
    }

    private void verifyConstructor(final Constructor<?> constructor) {
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor must be private: " + constructor);
    }

    @SneakyThrows
    private Set<Class<?>> getAllVentaComponents() {
        return ComponentUtil.scan(VentaEngine.class.getPackageName());
    }
}
