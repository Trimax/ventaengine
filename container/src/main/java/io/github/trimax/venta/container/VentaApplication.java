package io.github.trimax.venta.container;

import io.github.trimax.venta.container.exceptions.CyclicDependencyException;
import io.github.trimax.venta.container.exceptions.InjectionConstructorNotFoundException;
import io.github.trimax.venta.container.utils.ComponentUtil;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.container.utils.MeasurementUtil;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public final class VentaApplication {
    /** Components in creation order, so they are shut down in reverse order */
    private static final Map<Class<?>, Object> components = new LinkedHashMap<>();
    private static final Deque<Class<?>> creationStack = new ArrayDeque<>();

    /** Sorted by class name, so components and injected lists are created in the same order on every run */
    private static final Set<Class<?>> knownComponents = new TreeSet<>(Comparator.comparing(Class::getName));

    public static <T, A extends AbstractVentaApplication<T>> void run(final String[] args, @NonNull final Class<A> appClass, final T argument) {
        MeasurementUtil.measure("VentaApplication startup", () -> VentaApplication.createContext(appClass));

        final var component = getComponent(appClass);
        if (component == null)
            log.warn("The component {} wasn't found", appClass.getSimpleName());

        try {
            if (component != null)
                component.start(args, argument);
        } finally {
            MeasurementUtil.measure("VentaApplication shutdown", VentaApplication::shutdown);
        }
    }

    public static <T, R extends AbstractVentaApplication<T>> void run(final String[] args, @NonNull final Class<R> appClass) {
        run(args, appClass, null);
    }

    private static void createContext(@NonNull final Class<?> appClass) {
        // The state is static: a previous run (e.g. another test) must not leak components into this one
        components.clear();
        creationStack.clear();
        knownComponents.clear();
        knownComponents.addAll(ComponentUtil.scan(appClass.getPackageName()));

        StreamEx.of(knownComponents).forEach(VentaApplication::createComponent);
        log.debug("Found components: {}", StreamEx.of(components.keySet()).map(Class::getSimpleName).joining(","));
        log.info("{} components found and loaded", components.size());
    }

    public static <C> C getComponent(final Class<C> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    @SneakyThrows
    private static Object createComponent(final Class<?> clazz) {
        if (components.containsKey(clazz))
            return components.get(clazz);

        if (creationStack.contains(clazz))
            throw new CyclicDependencyException(StreamEx.of(creationStack).append(clazz).joining(" -> "));

        creationStack.add(clazz);
        try {
            final var constructor = findConstructor(clazz);
            constructor.setAccessible(true);

            final var instance = constructor.newInstance(resolveParameters(constructor));
            components.put(clazz, instance);
            EventUtil.register(instance);
        } finally {
            creationStack.remove(clazz);
        }

        final var instance = components.get(clazz);

        log.debug("Component {} was created successfully", clazz.getSimpleName());
        return instance;
    }

    private static Constructor<?> findConstructor(final Class<?> clazz) {
        final var constructor = ComponentUtil.findInjectConstructor(clazz);
        if (constructor != null)
            return constructor;

        final var constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1)
            return constructors[0];

        throw new InjectionConstructorNotFoundException(StreamEx.of(creationStack).append(clazz).joining(" -> "));
    }

    private static Object[] resolveParameters(final Constructor<?> constructor) {
        final var genericTypes = constructor.getGenericParameterTypes();
        final var rawTypes = constructor.getParameterTypes();

        final var params = new Object[genericTypes.length];
        for (int i = 0; i < genericTypes.length; i++)
            params[i] = resolveParameter(rawTypes[i], genericTypes[i]);

        return params;
    }

    private static Object resolveParameter(final Class<?> rawType, final Type genericType) {
        if (rawType == List.class && genericType instanceof ParameterizedType parameterizedType)
            return createComponentsList(parameterizedType);

        return createComponent(rawType);
    }

    private static List<Object> createComponentsList(final ParameterizedType parameterizedType) {
        final var rawClass = ComponentUtil.getRawClass(parameterizedType);
        if (rawClass == null)
            return Collections.emptyList();

        return StreamEx.of(knownComponents)
                .filter(rawClass::isAssignableFrom)
                .map(VentaApplication::createComponent)
                .toList();
    }

    private static void shutdown() {
        // Dependents are created after their dependencies, so they are closed first
        final var created = new ArrayList<>(components.values());
        Collections.reverse(created);
        created.forEach(VentaApplication::shutdownComponent);
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
