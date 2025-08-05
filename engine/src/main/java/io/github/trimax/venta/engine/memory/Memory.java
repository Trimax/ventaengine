package io.github.trimax.venta.engine.memory;

import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30C;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Memory {
    private final AnnotatedCache<Integer> vertexArrays = new AnnotatedCache<>(GL30C::glGenVertexArrays, GL30C::glDeleteVertexArrays);
    private final AnnotatedCache<Integer> textures = new AnnotatedCache<>(GL30C::glGenTextures, GL30C::glDeleteTextures);
    private final AnnotatedCache<Integer> programs = new AnnotatedCache<>(GL30C::glCreateProgram, GL30C::glDeleteProgram);
    private final AnnotatedCache<Integer> buffers = new AnnotatedCache<>(GL30C::glGenBuffers, GL30C::glDeleteBuffers);
    private final AnnotatedCache<Long> windows = new AnnotatedCache<>(new DefaultSupplier<>(), GLFW::glfwDestroyWindow);

    public void cleanup() {
        this.vertexArrays.cleanup();
        this.programs.cleanup();
        this.textures.cleanup();
        this.buffers.cleanup();
        this.windows.cleanup();
    }

    private static final class DefaultSupplier<T> implements Supplier<T> {
        @Override
        public T get() {
            throw new RuntimeException("Default supplier is not defined");
        }
    }
}
