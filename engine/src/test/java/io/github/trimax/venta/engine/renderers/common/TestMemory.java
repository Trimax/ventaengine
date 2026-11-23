package io.github.trimax.venta.engine.renderers.common;

import java.lang.reflect.Constructor;

import io.github.trimax.venta.engine.memory.Memory;

/**
 * The memory of the engine for the tests with OpenGL: it is a component with a private constructor.
 */
final class TestMemory {
    private TestMemory() {
    }

    static Memory create() throws Exception {
        final Constructor<Memory> constructor = Memory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        return constructor.newInstance();
    }
}
