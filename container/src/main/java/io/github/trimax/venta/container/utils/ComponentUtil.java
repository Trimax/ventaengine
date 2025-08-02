package io.github.trimax.venta.container.utils;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.annotations.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public final class ComponentUtil {
    public Constructor<?> findInjectConstructor(@NonNull final Class<?> clazz) {
        return StreamEx.of(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElse(null);
    }

    public Class<?> getRawClass(@NonNull final ParameterizedType parameterizedType) {
        if (ArrayUtils.isEmpty(parameterizedType.getActualTypeArguments()))
            return null;

        return switch (parameterizedType.getActualTypeArguments()[0]) {
            case Class<?> aClass -> aClass;

            case ParameterizedType type -> (Class<?>) type.getRawType();

            case WildcardType wildcardType -> {
                final var upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length == 1 && upperBounds[0] instanceof Class<?> upperClass)
                    yield upperClass;

                yield null;
            }

            default -> null;
        };
    }

    @SneakyThrows
    public Set<Class<?>> scan(final String basePackage) {
        final Set<Class<?>> classes = new HashSet<>();
        final String path = basePackage.replace('.', '/');

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final Enumeration<URL> resources = classLoader.getResources(path);

        StreamEx.of(resources.asIterator())
                .map(URL::getFile)
                .map(File::new)
                .filter(File::exists)
                .forEach(directory -> scanDirectoryRecursively(directory, basePackage, classes));

        return classes;
    }

    @SneakyThrows
    private void scanDirectoryRecursively(final File dir, final String packageName, final Set<Class<?>> classes) {
        StreamEx.of(Objects.requireNonNull(dir.listFiles())).forEach(file -> processDirectory(file, packageName, classes));
    }

    @SneakyThrows
    private static void processDirectory(final File file, final String packageName, final Set<Class<?>> classes) {
        if (file.isDirectory()) {
            scanDirectoryRecursively(file, packageName + "." + file.getName(), classes);
            return;
        }

        if (!file.getName().endsWith(".class"))
            return;

        final var cls = Class.forName(packageName + "." + file.getName().replace(".class", ""));
        if (cls.isAnnotationPresent(Component.class))
            classes.add(cls);
    }
}
