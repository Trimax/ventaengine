package io.github.trimax.venta.engine.managers;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleManager extends AbstractManager<ConsoleManager.ConsoleEntity, ConsoleView> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ProgramManager programManager;

    public ConsoleView create(final String name) {
        log.debug("Creating console {}", name);

        final int consoleVertexArrayObjectID = glGenVertexArrays();
        final int consoleVerticesBufferID = glGenBuffers();

        glBindVertexArray(consoleVertexArrayObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, consoleVerticesBufferID);

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

        final int characterVertexArrayObjectID = glGenVertexArrays();
        final int characterVerticesBufferID = glGenBuffers();

        glBindVertexArray(characterVertexArrayObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, characterVerticesBufferID);

        // layout(location=0): vec2 aPos; layout(location=1): vec2 textureCoordinates;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return store(new ConsoleManager.ConsoleEntity(name, programAccessor.get(programManager.load("console")),
                consoleVertexArrayObjectID, consoleVerticesBufferID,
                characterVertexArrayObjectID, characterVerticesBufferID));
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Override
    protected void destroy(final ConsoleEntity console) {
        log.debug("Destroying console {} ({})", console.getID(), console.getName());
        glDeleteVertexArrays(console.characterVertexArrayObjectID);
        glDeleteBuffers(console.characterVerticesBufferID);

        glDeleteVertexArrays(console.consoleVertexArrayObjectID);
        glDeleteBuffers(console.consoleVerticesBufferID);
    }

    @Getter
    public static final class ConsoleEntity extends AbstractEntity implements ConsoleView {
        private final StringBuilder inputBuffer = new StringBuilder();
        private final List<String> history = new ArrayList<>();

        private final ProgramManager.ProgramEntity program;

        private final int consoleVertexArrayObjectID;
        private final int consoleVerticesBufferID;

        private final int characterVertexArrayObjectID;
        private final int characterVerticesBufferID;

        private boolean visible;

        ConsoleEntity(final String name,
                @NonNull final ProgramManager.ProgramEntity program,
                final int consoleVertexArrayObjectID,
                final int consoleVerticesBufferID,
                final int characterVertexArrayObjectID,
                final int characterVerticesBufferID) {
            super(name);

            this.consoleVertexArrayObjectID = consoleVertexArrayObjectID;
            this.consoleVerticesBufferID = consoleVerticesBufferID;
            this.characterVertexArrayObjectID = characterVertexArrayObjectID;
            this.characterVerticesBufferID = characterVerticesBufferID;

            this.program = program;
        }

        public void toggle() {
            visible = !visible;
        }

        public void accept(final char c) {
            inputBuffer.append(c);
        }

        public boolean handle(final int key) {
            return switch (key) {
                case GLFW_KEY_ENTER -> {
                    submit();
                    yield true;
                }
                case GLFW_KEY_BACKSPACE -> {
                    backspace();
                    yield true;
                }
                default -> false;
            };
        }

        private void backspace() {
            if (!inputBuffer.isEmpty())
                inputBuffer.setLength(inputBuffer.length() - 1);
        }

        private void submit() {
            final var command = inputBuffer.toString();
            history.add(command);

            execute(command);
            inputBuffer.setLength(0);
        }

        private void execute(final String command) {
            log.info("Command for execution: {}", command);
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ConsoleAccessor extends AbstractAccessor {}
}
