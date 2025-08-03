package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.managers.ConsoleManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.ConsoleEntity;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleManagerImplementation
        extends AbstractManagerImplementation<ConsoleEntity, ConsoleView>
        implements ConsoleManager {
    private final ConsoleItemManagerImplementation consoleItemManager;
    private final ProgramManagerImplementation programManager;
    private final Memory memory;

    public ConsoleEntity create(final String name) {
        log.debug("Creating console {}", name);

        final var consoleVertexArrayObject = memory.getVertexArrays().create("Console %s VAO", name);
        final var consoleVerticesBuffer = memory.getBuffers().create("Console %s vertex buffer", name);

        glBindVertexArray(consoleVertexArrayObject.getData());
        glBindBuffer(GL_ARRAY_BUFFER, consoleVerticesBuffer.getData());

        final float[] vertices = {
                /* Top-left */    -1.0f, 1.0f,
                /* Top-right */    1.0f, 1.0f,
                /* Bottom-right */ 1.0f, 0.0f,
                /* Bottom-left */ -1.0f, 0.0f};

        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return store(new ConsoleEntity(name,
                consoleItemManager.create(),
                programManager.load("console"),
                consoleVertexArrayObject, consoleVerticesBuffer));
    }

    @Override
    protected void destroy(final ConsoleEntity console) {
        log.debug("Destroying console {} ({})", console.getID(), console.getName());

        memory.getVertexArrays().delete(console.getVertexArrayObject());
        memory.getBuffers().delete(console.getVerticesBuffer());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    public record ConsoleMessage(ConsoleMessageType type, String text) {}
}
