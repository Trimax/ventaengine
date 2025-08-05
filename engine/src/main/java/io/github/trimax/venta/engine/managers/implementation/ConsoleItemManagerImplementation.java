package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.ConsoleItemManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.ConsoleItemEntity;
import io.github.trimax.venta.engine.model.view.ConsoleItemView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleItemManagerImplementation
        extends AbstractManagerImplementation<ConsoleItemEntity, ConsoleItemView>
        implements ConsoleItemManager {
    private final ProgramManagerImplementation programManager;
    private final FontManagerImplementation fontManager;
    private final Memory memory;

    public ConsoleItemEntity create() {
        log.debug("Creating console text");

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

        return store(new ConsoleItemEntity("SHARED",
                programManager.load("text"),
                fontManager.load("DejaVuSansMono"),
                vertexArrayObjectID, verticesBufferID));
    }

    @Override
    protected void destroy(final ConsoleItemEntity consoleItem) {
        log.debug("Destroying console item {} ({})", consoleItem.getID(), consoleItem.getName());
        memory.getVertexArrays().delete(consoleItem.getVertexArrayObjectID());
        memory.getBuffers().delete(consoleItem.getVerticesBufferID());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
