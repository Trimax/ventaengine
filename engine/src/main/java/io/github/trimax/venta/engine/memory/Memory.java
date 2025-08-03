package io.github.trimax.venta.engine.memory;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lwjgl.opengl.GL30C;

@Getter
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Memory {
    private final AnnotatedCache<Integer> vertexArrays = new AnnotatedCache<>(GL30C::glGenVertexArrays, GL30C::glDeleteVertexArrays);
    private final AnnotatedCache<Integer> buffers = new AnnotatedCache<>(GL30C::glGenBuffers, GL30C::glDeleteBuffers);

    public void cleanup() {
        this.vertexArrays.cleanup();
        this.buffers.cleanup();
    }
}
