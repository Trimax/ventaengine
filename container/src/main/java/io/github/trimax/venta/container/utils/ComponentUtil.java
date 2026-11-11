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
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@UtilityClass
public final class ComponentUtil {
    private static final String CLASS_FILE_SUFFIX = ".class";

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

    public Set<Class<?>> scan(final String basePackage) {
        return scan(basePackage, Thread.currentThread().getContextClassLoader());
    }

    @SneakyThrows
    Set<Class<?>> scan(@NonNull final String basePackage, @NonNull final ClassLoader classLoader) {
        final Set<Class<?>> classes = new HashSet<>();
        final String path = basePackage.replace('.', '/');

        for (final URL resource : Collections.list(classLoader.getResources(path)))
            switch (resource.getProtocol()) {
                case "file" -> scanDirectory(Path.of(resource.toURI()), basePackage, classLoader, classes);
                case "jar" -> scanJar(resource, path, classLoader, classes);
                default -> {
                }
            }

        return classes;
    }

    @SneakyThrows
    private void scanDirectory(final Path directory, final String basePackage, final ClassLoader classLoader, final Set<Class<?>> classes) {
        if (!Files.isDirectory(directory))
            return;

        try (final var files = Files.walk(directory)) {
            files.filter(Files::isRegularFile)
                    .map(file -> directory.relativize(file).toString().replace(File.separatorChar, '/'))
                    .forEach(name -> processClassFile(basePackage.replace('.', '/') + "/" + name, classLoader, classes));
        }
    }

    @SneakyThrows
    private void scanJar(final URL resource, final String path, final ClassLoader classLoader, final Set<Class<?>> classes) {
        final var jarFileURL = ((JarURLConnection) resource.openConnection()).getJarFileURL();

        try (final JarFile jar = new JarFile(Path.of(jarFileURL.toURI()).toFile())) {
            StreamEx.of(jar.entries().asIterator())
                    .map(JarEntry::getName)
                    .filter(name -> name.startsWith(path + "/"))
                    .forEach(name -> processClassFile(name, classLoader, classes));
        }
    }

    @SneakyThrows
    private void processClassFile(final String resourceName, final ClassLoader classLoader, final Set<Class<?>> classes) {
        if (!resourceName.endsWith(CLASS_FILE_SUFFIX) || resourceName.contains("-"))
            return;

        final var className = resourceName.substring(0, resourceName.length() - CLASS_FILE_SUFFIX.length()).replace('/', '.');
        final var cls = Class.forName(className, true, classLoader);
        if (cls.isAnnotationPresent(Component.class))
            classes.add(cls);
    }
}
