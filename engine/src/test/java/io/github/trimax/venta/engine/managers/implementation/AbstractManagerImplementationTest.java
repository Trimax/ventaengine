package io.github.trimax.venta.engine.managers.implementation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.events.InstanceDeletedEvent;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;

final class AbstractManagerImplementationTest {
    @Test
    void testDeleteDestroysInstanceOnlyOnce() {
        final var manager = new TestManager();
        final var instance = manager.create("instance");

        manager.remove(instance);
        manager.remove(instance);

        assertEquals(List.of(instance), manager.destroyed, "Instance must be destroyed exactly once");
        assertNull(manager.getInstance(instance.getID()), "Deleted instance must not be accessible");
    }

    @Test
    void testDeleteIgnoresInstanceWhichIsNotRegistered() {
        final var manager = new TestManager();
        final var registered = manager.create("instance");
        final var stranger = new TestInstance(registered.getID());

        manager.remove(stranger);

        assertTrue(manager.destroyed.isEmpty(), "Not registered instance must not be destroyed");
        assertSame(registered, manager.getInstance(registered.getID()), "Registered instance must be kept");
    }

    @Test
    void testDeletePostsEventOnceBeforeDestroy() {
        final var manager = new TestManager();
        final var instance = manager.create("instance");
        final var listener = new DeleteListener(manager, instance);
        EventUtil.register(listener);

        manager.remove(instance);
        manager.remove(instance);

        assertEquals(1, listener.events, "Event must be posted exactly once");
        assertTrue(listener.postedBeforeDestroy, "Event must be posted before the instance is destroyed");
    }

    @Test
    void testCleanupDestroysAllInstances() {
        final var manager = new TestManager();
        manager.create("first");
        manager.create("second");

        manager.cleanup();

        assertEquals(2, manager.destroyed.size(), "All instances must be destroyed");
        assertFalse(manager.instanceIterator().hasNext(), "Manager must be empty after cleanup");
    }

    private static final class TestInstance implements AbstractInstance {
        private final String id;

        private TestInstance(final String id) {
            this.id = id;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public String getName() {
            return id;
        }
    }

    private static final class DeleteListener {
        private final TestManager manager;
        private final TestInstance instance;
        private boolean postedBeforeDestroy;
        private int events;

        private DeleteListener(final TestManager manager, final TestInstance instance) {
            this.manager = manager;
            this.instance = instance;
        }

        @Subscribe
        public void onInstanceDeleted(final InstanceDeletedEvent event) {
            if (event.instance() != instance)
                return;

            events++;
            postedBeforeDestroy = manager.destroyed.isEmpty();
        }
    }

    private static final class TestManager extends AbstractManagerImplementation<TestInstance, TestInstance> {
        private final List<TestInstance> destroyed = new ArrayList<>();

        private TestInstance create(final String id) {
            return store(new TestInstance(id));
        }

        private void remove(final TestInstance instance) {
            delete(instance);
        }

        @Override
        protected void destroy(final TestInstance entity) {
            destroyed.add(entity);
        }
    }
}
