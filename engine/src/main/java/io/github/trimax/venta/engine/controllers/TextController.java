package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.states.TextState;
import io.github.trimax.venta.engine.registries.implementation.FontRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextController extends AbstractController<TextState, Void> {
    private final ProgramRegistryImplementation programRegistry;
    private final FontRegistryImplementation fontRegistry;
    private final Memory memory;

    @Override
    protected TextState create(final Void argument) {
        log.debug("Initializing text");

        final int vertexArrayObjectID = memory.getVertexArrays().create("Console text VAO");
        final int verticesBufferID = memory.getBuffers().create("Console text vertex buffer");

        glBindVertexArray(vertexArrayObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, verticesBufferID);

        // layout(location=0): vec2 aPos; layout(location=1): vec2 textureCoordinates;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new TextState(programRegistry.get("text.json"),
                fontRegistry.get("DejaVuSansMono"),
                vertexArrayObjectID, verticesBufferID);
    }

    @Override
    protected void destroy(@NonNull final TextState text) {
        log.debug("Deinitializing text");

        memory.getVertexArrays().delete(text.getVertexArrayObjectID());
        memory.getBuffers().delete(text.getVerticesBufferID());
    }
}
