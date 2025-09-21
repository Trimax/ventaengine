package io.github.trimax.venta.engine.memory;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.GL30C;

import java.util.function.Supplier;

@Getter
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Memory {
    private final AnnotatedCache<Integer> vertexArrays = new AnnotatedCache<>(GL30C::glGenVertexArrays, GL30C::glDeleteVertexArrays);
    private final AnnotatedCache<Integer> textures = new AnnotatedCache<>(GL30C::glGenTextures, GL30C::glDeleteTextures);
    private final AnnotatedCache<Integer> programs = new AnnotatedCache<>(GL30C::glCreateProgram, GL30C::glDeleteProgram);
    private final AnnotatedCache<Integer> buffers = new AnnotatedCache<>(GL30C::glGenBuffers, GL30C::glDeleteBuffers);
    private final AnnotatedCache<Long> windows = new AnnotatedCache<>(new DefaultSupplier<>(), GLFW::glfwDestroyWindow);
    private final AnnotatedCache<Integer> audioBuffers = new AnnotatedCache<>(AL10::alGenBuffers, AL10::alDeleteBuffers);
    private final AnnotatedCache<Integer> audioSources = new AnnotatedCache<>(AL10::alGenSources, AL10::alDeleteSources);

    public void cleanup() {
        this.vertexArrays.cleanup();
        this.programs.cleanup();
        this.textures.cleanup();
        this.buffers.cleanup();
        this.windows.cleanup();
        this.audioBuffers.cleanup();
        this.audioSources.cleanup();
    }

    //TODO: Create geometry, clean geometry

    private static final class DefaultSupplier<T> implements Supplier<T> {
        @Override
        public T get() {
            throw new RuntimeException("Default supplier is not defined");
        }
    }
}
