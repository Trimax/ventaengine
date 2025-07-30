package io.github.trimax.venta.container.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.github.trimax.venta.container.annotations.Component;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class ComponentUtil {
    public Set<Class<?>> scan(final String basePackage) throws IOException {
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
