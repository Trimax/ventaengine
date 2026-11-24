package io.github.trimax.venta.engine.memory;

import java.lang.reflect.Constructor;

/**
 * The memory of the engine for the tests with OpenGL: it is a component with a private constructor.
 */
public final class TestMemory {
    private TestMemory() {
    }

    public static Memory create() throws Exception {
        final Constructor<Memory> constructor = Memory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        return constructor.newInstance();
    }
}
