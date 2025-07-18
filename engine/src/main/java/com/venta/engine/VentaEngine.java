package com.venta.engine;

import com.venta.engine.annotations.Inject;
import com.venta.engine.core.Context;
import com.venta.engine.core.Engine;
import com.venta.engine.exception.EngineInitializationException;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.utils.ComponentUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.lang.reflect.Constructor;
import java.util.*;

@Slf4j
@UtilityClass
public final class VentaEngine {
    private static final Map<Class<?>, Object> components = new HashMap<>();
    private static final Deque<Class<?>> creationStack = new ArrayDeque<>();

    public static void run(final String[] args, final Venta venta) {
        log.info("Starting VentaEngine");

        final long timeStarted = System.currentTimeMillis();
        createContext();

        final var engine = getComponent(Engine.class);
        engine.initialize(venta.createWindowConfiguration());

        final long timeFinished = System.currentTimeMillis();
        log.info("VentaEngine started in {}ms", timeFinished - timeStarted);

        venta.onStartup(args, getComponent(Context.class));

        engine.setVenta(venta);
        engine.run();
    }

    private static void createContext() {
        try {
            StreamEx.of(ComponentUtil.scan(VentaEngine.class.getPackageName())).forEach(VentaEngine::createComponent);
            log.info("{} components found and loaded", components.size());
            log.debug("Found components: {}", StreamEx.of(components.keySet()).map(Class::getSimpleName).joining(","));
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

        if (creationStack.contains(clazz)) {
            throw new EngineInitializationException("Cyclic dependency: " + StreamEx.of(creationStack).append(clazz).joining(" -> "));
        }
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
        if (constructors.length == 1) {
            return constructors[0];
        }

        throw new EngineInitializationException("Cannot determine which constructor to use for " + clazz.getName()
                + ". Use @Inject to mark the constructor explicitly.");
    }

    private Constructor<?> findInjectConstructor(final Class<?> clazz) {
        return StreamEx.of(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElse(null);
    }
}
