package com.venta.engine;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.venta.engine.annotations.Inject;
import com.venta.engine.core.Context;
import com.venta.engine.core.Engine;
import com.venta.engine.exceptions.EngineInitializationException;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.utils.ComponentUtil;
import com.venta.engine.utils.MeasurementUtil;
import com.venta.engine.utils.ResourceUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@UtilityClass
public final class VentaEngine {
    private static final Map<Class<?>, Object> components = new HashMap<>();
    private static final Deque<Class<?>> creationStack = new ArrayDeque<>();

    public static void run(final String[] args, final Venta venta) {
        log.info("Starting VentaEngine {}", ResourceUtil.load("/banner.txt"));

        final var engine = MeasurementUtil.measure("VentaEngine startup", () -> createEngine(venta));
        venta.onStartup(args, getComponent(Context.class));

        engine.run();
        MeasurementUtil.measure("VentaEngine shutdown", VentaEngine::shutdownEngine);
    }

    private static Engine createEngine(final Venta venta) {
        createContext();

        final var engine = getComponent(Engine.class);
        engine.initialize(venta);

        return engine;
    }

    private static void createContext() {
        try {
            StreamEx.of(ComponentUtil.scan(VentaEngine.class.getPackageName())).forEach(VentaEngine::createComponent);
            log.debug("Found components: {}", StreamEx.of(components.keySet()).map(Class::getSimpleName).joining(","));
            log.info("{} components found and loaded", components.size());
        } catch (final Exception e) {
            throw new EngineInitializationException(e.getMessage());
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
            throw new EngineInitializationException("Cyclic dependency: " + StreamEx.of(creationStack).append(clazz).joining(" -> "));

        creationStack.add(clazz);

        final var constructor = findConstructor(clazz);
        constructor.setAccessible(true);

        final var instance = constructor.newInstance(Arrays.stream(constructor.getParameterTypes()).map(VentaEngine::createComponent).toArray());
        components.put(clazz, instance);

        creationStack.remove(clazz);

        log.debug("Component {} was created successfully", clazz.getSimpleName());
        return instance;
    }

    private Constructor<?> findConstructor(final Class<?> clazz) {
        final var constructor = findInjectConstructor(clazz);
        if (constructor != null)
            return constructor;

        final var constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1)
            return constructors[0];

        throw new EngineInitializationException("Cannot determine which constructor to use for " + clazz.getName()
                + ". Use @" + Inject.class.getSimpleName() + " to mark the constructor explicitly.");
    }

    private Constructor<?> findInjectConstructor(final Class<?> clazz) {
        return StreamEx.of(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElse(null);
    }

    private void shutdownEngine() {
        for (final Object component : components.values()) {
            if (component instanceof AutoCloseable)
                try {
                    ((AutoCloseable) component).close();
                } catch (final Exception e) {
                    log.warn("Can't shutdown component {}", component.getClass().getSimpleName(), e);
                }
        }
    }
}
