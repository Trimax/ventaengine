package com.venta.container;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.venta.container.annotations.Inject;
import com.venta.container.exceptions.ContextInitializationException;
import com.venta.container.utils.ComponentUtil;

import com.venta.container.utils.MeasurementUtil;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
public final class VentaApplication {
    private static final Map<Class<?>, Object> components = new HashMap<>();
    private static final Deque<Class<?>> creationStack = new ArrayDeque<>();

    public static <T, A extends AbstractVentaApplication<T>> void run(final String[] args, @NonNull final Class<A> appClass, final T argument) {
        MeasurementUtil.measure("VentaApplication startup", () -> VentaApplication.createContext(appClass));

        final var component = getComponent(appClass);
        if (component != null)
            component.start(args, argument);
        else
            log.warn("The component wasn't found:", appClass.getSimpleName());
        MeasurementUtil.measure("VentaApplication shutdown", VentaApplication::shutdown);
    }

    public static <T, R extends AbstractVentaApplication<T>> void run(final String[] args, @NonNull final Class<R> appClass) {
        run(args, appClass, null);
    }

    private static void createContext(@NonNull final Class<?> appClass) {
        try {
            StreamEx.of(ComponentUtil.scan(appClass.getPackageName())).forEach(VentaApplication::createComponent);
            log.debug("Found components: {}", StreamEx.of(components.keySet()).map(Class::getSimpleName).joining(","));
            log.info("{} components found and loaded", components.size());
        } catch (final Exception e) {
            throw new ContextInitializationException(e.getMessage());
        }
    }

    private static <C> C getComponent(final Class<C> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    @SneakyThrows
    private static Object createComponent(final Class<?> clazz) {
        if (components.containsKey(clazz))
            return components.get(clazz);

        if (creationStack.contains(clazz))
            throw new ContextInitializationException(
                    "Cyclic dependency: " + StreamEx.of(creationStack).append(clazz).joining(" -> "));

        creationStack.add(clazz);

        final var constructor = findConstructor(clazz);
        constructor.setAccessible(true);

        final var instance = constructor.newInstance(
                Arrays.stream(constructor.getParameterTypes()).map(VentaApplication::createComponent).toArray());
        components.put(clazz, instance);

        creationStack.remove(clazz);

        log.debug("Component {} was created successfully", clazz.getSimpleName());
        return instance;
    }

    private static Constructor<?> findConstructor(final Class<?> clazz) {
        final var constructor = findInjectConstructor(clazz);
        if (constructor != null)
            return constructor;

        final var constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1)
            return constructors[0];

        throw new ContextInitializationException("Cannot determine which constructor to use for " + clazz.getName()
                + ". Use @" + Inject.class.getSimpleName() + " to mark the constructor explicitly.");
    }

    private static Constructor<?> findInjectConstructor(final Class<?> clazz) {
        return StreamEx.of(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElse(null);
    }

    private static void shutdown() {
        StreamEx.of(components.values()).forEach(VentaApplication::shutdownComponent);
        components.clear();
    }

    private static void shutdownComponent(final Object component) {
        if (component instanceof AutoCloseable closeable)
            try {
                closeable.close();
            } catch (final Exception e) {
                log.warn("Can't shutdown component {}", component.getClass().getSimpleName(), e);
            }
    }
}
