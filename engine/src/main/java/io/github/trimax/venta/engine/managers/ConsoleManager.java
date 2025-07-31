package io.github.trimax.venta.engine.managers;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleManager extends AbstractManager<ConsoleManager.ConsoleEntity, ConsoleView> {
    public ConsoleView create(final String name) {
        log.debug("Creating console {}", name);

        final int vertexArrayObjectID = glGenVertexArrays();
        final int verticesBufferID = glGenBuffers();

        glBindVertexArray(vertexArrayObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, verticesBufferID);

        final float[] vertices = {
                -1.0f, 1.0f,   // top-left
                1.0f, 1.0f,   // top-right
                1.0f, 0.0f,   // bottom-right
                -1.0f, 0.0f    // bottom-left
        };
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return store(new ConsoleManager.ConsoleEntity(name, vertexArrayObjectID, verticesBufferID));
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Override
    protected void destroy(final ConsoleEntity console) {
        log.debug("Destroying console {} ({})", console.getID(), console.getName());
        glDeleteVertexArrays(console.vertexArrayObjectID);
        glDeleteBuffers(console.verticesBufferID);
    }

    @Getter
    public static final class ConsoleEntity extends AbstractEntity implements ConsoleView {
        private final int vertexArrayObjectID;
        private final int verticesBufferID;

        ConsoleEntity(final String name, int vertexArrayObjectID, int verticesBufferID) {
            super(name);
            this.vertexArrayObjectID = vertexArrayObjectID;
            this.verticesBufferID = verticesBufferID;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ConsoleAccessor extends AbstractAccessor {}
}
