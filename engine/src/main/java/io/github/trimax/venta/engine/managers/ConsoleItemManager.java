package io.github.trimax.venta.engine.managers;

import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.model.view.ConsoleItemView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleItemManager extends AbstractManager<ConsoleItemManager.ConsoleItemEntity, ConsoleItemView> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final FontManager.FontAccessor fontAccessor;
    private final ProgramManager programManager;
    private final FontManager fontManager;

    public ConsoleItemView create() {
        log.debug("Creating console item");

        final int vertexArrayObjectID = glGenVertexArrays();
        final int verticesBufferID = glGenBuffers();

        glBindVertexArray(vertexArrayObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, verticesBufferID);

        // layout(location=0): vec2 aPos; layout(location=1): vec2 textureCoordinates;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return store(new ConsoleItemManager.ConsoleItemEntity("SHARED",
                programAccessor.get(programManager.load("text")),
                fontAccessor.get(fontManager.create("DejaVuSansMono")),
                vertexArrayObjectID, verticesBufferID));
    }

    @Override
    protected void destroy(final ConsoleItemEntity consoleItem) {
        log.debug("Destroying console item {} ({})", consoleItem.getID(), consoleItem.getName());
        glDeleteVertexArrays(consoleItem.vertexArrayObjectID);
        glDeleteBuffers(consoleItem.verticesBufferID);
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ConsoleItem;
    }

    @Getter
    public static final class ConsoleItemEntity extends AbstractEntity implements ConsoleItemView {
        private final ProgramManager.ProgramEntity program;
        private final FontManager.FontEntity font;

        private final int vertexArrayObjectID;
        private final int verticesBufferID;

        ConsoleItemEntity(final String name,
                @NonNull final ProgramManager.ProgramEntity program,
                @NonNull final FontManager.FontEntity font,
                final int vertexArrayObjectID, final int verticesBufferID) {
            super(name);

            this.vertexArrayObjectID = vertexArrayObjectID;
            this.verticesBufferID = verticesBufferID;

            this.program = program;
            this.font = font;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ConsoleItemAccessor extends AbstractAccessor {}
}
