package io.github.trimax.venta.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.utils.ComponentUtil;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

public final class ComponentAccessibilityTest {
    @Test
    public void test() {
        StreamEx.of(getAllVentaComponents()).forEach(this::verifyComponent);
    }

    private void verifyComponent(final Class<?> component) {
        StreamEx.of(component.getDeclaredConstructors()).forEach(this::verifyConstructor);
    }

    private void verifyConstructor(final Constructor<?> constructor) {
        assertFalse(Modifier.isProtected(constructor.getModifiers()), "Constructor must be private: " + constructor);
        assertFalse(Modifier.isPublic(constructor.getModifiers()), "Constructor must be private: " + constructor);
    }

    @SneakyThrows
    private Set<Class<?>> getAllVentaComponents() {
        return ComponentUtil.scan(VentaEngine.class.getPackageName());
    }
}
