package io.github.trimax.venta.engine.registries.implementation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import lombok.NonNull;

final class AbstractRegistryImplementationTest {
    @Test
    void testEntityIsLoadedOnce() {
        final var registry = new TestRegistry();

        final var first = registry.get("a.json");
        final var second = registry.get("a.json");

        assertSame(first, second, "Cached entity must be returned");
        assertEquals(List.of("a.json:null"), registry.loaded, "Entity must be loaded once");
    }

    @Test
    void testArgumentIsPartOfTheKey() {
        final var registry = new TestRegistry();

        final var vertex = registry.get("shader.glsl", "Vertex");
        final var fragment = registry.get("shader.glsl", "Fragment");

        assertNotSame(vertex, fragment, "Different arguments must give different entities");
        assertEquals("shader.glsl:Vertex", vertex.getID(), "Vertex entity");
        assertEquals("shader.glsl:Fragment", fragment.getID(), "Fragment entity");
        assertSame(vertex, registry.get("shader.glsl", "Vertex"), "The same argument must return the cached entity");
    }

    @Test
    void testCleanupUnloadsAllEntities() {
        final var registry = new TestRegistry();
        registry.get("a.json");
        registry.get("b.json", "x");

        registry.cleanup();

        assertEquals(2, registry.unloaded.size(), "All entities must be unloaded");
        assertFalse(registry.iterator().hasNext(), "Registry must be empty after cleanup");
    }

    private record TestEntity(String getID) implements AbstractEntity {}

    private static final class TestRegistry extends AbstractRegistryImplementation<TestEntity, TestEntity, String> {
        private final List<String> loaded = new ArrayList<>();
        private final List<TestEntity> unloaded = new ArrayList<>();

        @Override
        protected TestEntity load(@NonNull final String resourcePath, final String argument) {
            final var id = resourcePath + ":" + argument;
            loaded.add(id);
            return new TestEntity(id);
        }

        @Override
        protected void unload(@NonNull final TestEntity entity) {
            unloaded.add(entity);
        }
    }
}
