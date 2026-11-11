package io.github.trimax.venta.engine.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

final class AnnotatedCacheTest {
    private final AtomicInteger counter = new AtomicInteger();
    private final List<Integer> deleted = new ArrayList<>();
    private final AnnotatedCache<Integer> cache = new AnnotatedCache<>(counter::incrementAndGet, deleted::add);

    @Test
    void testNameWithPercentWithoutArguments() {
        assertDoesNotThrow(() -> cache.create("textures/fire%20.png"), "A name with '%' must not be used as a format");
    }

    @Test
    void testNameWithPercentAsArgument() {
        assertDoesNotThrow(() -> cache.create("Program %s", "programs/100%.json"), "A name with '%' must be safe as an argument");
    }

    @Test
    void testDeleteReleasesOnce() {
        final var resource = cache.create("Resource %s", "a");

        cache.delete(resource);
        cache.delete(resource);

        assertEquals(List.of(resource), deleted, "A resource must be released exactly once");
    }

    @Test
    void testCleanupReleasesLeakedResources() {
        final var first = cache.create("first");
        final var second = cache.create("second");
        cache.delete(first);

        cache.cleanup();

        assertEquals(List.of(first, second), deleted, "Cleanup must release resources that were not deleted");
    }
}
