package io.github.trimax.venta.container.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void testPackageScanInJar(@TempDir final Path tempDirectory) throws Exception {
        final var jar = tempDirectory.resolve("components.jar");
        try (final var output = new JarOutputStream(Files.newOutputStream(jar))) {
            for (final var classFile : getMockClassFiles()) {
                output.putNextEntry(new JarEntry(getMockPackagePath() + "/" + classFile.getFileName()));
                output.write(Files.readAllBytes(classFile));
                output.closeEntry();
            }
        }

        final var resource = new URL("jar:" + jar.toUri() + "!/" + getMockPackagePath());
        assertMockComponentsFound(ComponentUtil.scan(ComponentA.class.getPackageName(), new SingleResourceClassLoader(resource)));
    }

    @Test
    void testPackageScanInDirectoryWithSpaces(@TempDir final Path tempDirectory) throws Exception {
        final var directory = Files.createDirectories(tempDirectory.resolve("directory with spaces").resolve(getMockPackagePath()));
        for (final var classFile : getMockClassFiles())
            Files.copy(classFile, directory.resolve(classFile.getFileName()));

        final var resource = directory.toUri().toURL();
        assertTrue(resource.toString().contains("%20"), "Resource URL must be encoded");
        assertMockComponentsFound(ComponentUtil.scan(ComponentA.class.getPackageName(), new SingleResourceClassLoader(resource)));
    }

    @Test
    void testInjectConstructor() {
        assertNotNull(ComponentUtil.findInjectConstructor(ComponentWithInjectionConstructor.class), ComponentA.class.getSimpleName() + " must have injection constructor");
        assertNull(ComponentUtil.findInjectConstructor(ComponentWithoutInjectionConstructor.class), ComponentA.class.getSimpleName() + " must not have injection constructor");
    }

    private static void assertMockComponentsFound(final java.util.Set<Class<?>> components) {
        assertEquals(2, components.size(), "Components list must contain two components");
        assertTrue(components.contains(ComponentA.class), ComponentA.class.getSimpleName() + " should be present");
        assertTrue(components.contains(ComponentB.class), ComponentB.class.getSimpleName() + " should be present");
    }

    private static String getMockPackagePath() {
        return ComponentA.class.getPackageName().replace('.', '/');
    }

    private static List<Path> getMockClassFiles() throws Exception {
        final var directory = Path.of(ComponentA.class.getResource(ComponentA.class.getSimpleName() + ".class").toURI()).getParent();
        try (final var files = Files.list(directory)) {
            return files.filter(file -> file.toString().endsWith(".class")).toList();
        }
    }

    /** Exposes a single resource location and delegates class loading to the test class loader */
    private static final class SingleResourceClassLoader extends ClassLoader {
        private final URL resource;

        private SingleResourceClassLoader(final URL resource) {
            super(ComponentUtilTest.class.getClassLoader());
            this.resource = resource;
        }

        @Override
        public Enumeration<URL> getResources(final String name) throws IOException {
            return Collections.enumeration(List.of(resource));
        }
    }
}
