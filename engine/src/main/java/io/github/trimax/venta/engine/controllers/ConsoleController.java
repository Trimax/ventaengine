package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.ConsoleItemManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.ConsoleEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleController extends AbstractController<ConsoleEntity, Void> {
    private final ConsoleItemManagerImplementation consoleItemManager;
    private final ProgramManagerImplementation programManager;
    private final Memory memory;

    @Override
    protected ConsoleEntity create(final Void argument) {
        log.debug("Creating console");

        final int consoleVertexArrayObjectID = memory.getVertexArrays().create("Console VAO");
        final int consoleVerticesBufferID = memory.getBuffers().create("Console vertex buffer");

        glBindVertexArray(consoleVertexArrayObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, consoleVerticesBufferID);

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

        return new ConsoleEntity("REMOVE THIS NAME",
                consoleItemManager.create(),
                programManager.load("console"),
                consoleVertexArrayObjectID, consoleVerticesBufferID);
    }

    @Override
    protected void destroy(@NonNull final ConsoleEntity object) {
        log.debug("Destroying console");

        memory.getVertexArrays().delete(object.getVertexArrayObjectID());
        memory.getBuffers().delete(object.getVerticesBufferID());
    }
}
