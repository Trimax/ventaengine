package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleManager extends AbstractManager<ConsoleManager.ConsoleEntity, ConsoleView> {
    private final ConsoleItemManager.ConsoleItemAccessor consoleItemAccessor;
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ConsoleItemManager consoleItemManager;
    private final ProgramManager programManager;

    public ConsoleView create(final String name) {
        log.debug("Creating console {}", name);

        final int consoleVertexArrayObjectID = glGenVertexArrays();
        final int consoleVerticesBufferID = glGenBuffers();

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

        return store(new ConsoleManager.ConsoleEntity(name,
                consoleItemAccessor.get(consoleItemManager.create()),
                programAccessor.get(programManager.load("console")),
                consoleVertexArrayObjectID, consoleVerticesBufferID));
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
        private final StringBuilder inputBuffer = new StringBuilder(Definitions.CONSOLE_WELCOME_SYMBOL);
        private final List<ConsoleMessage> history = new ArrayList<>();

        private final ConsoleItemManager.ConsoleItemEntity consoleItem;
        private final ProgramManager.ProgramEntity program;

        private final int vertexArrayObjectID;
        private final int verticesBufferID;

        private boolean isVisible;

        ConsoleEntity(final String name,
                @NonNull final ConsoleItemManager.ConsoleItemEntity consoleItem,
                @NonNull final ProgramManager.ProgramEntity program,
                final int vertexArrayObjectID,
                final int verticesBufferID) {
            super(name);

            this.vertexArrayObjectID = vertexArrayObjectID;
            this.verticesBufferID = verticesBufferID;

            this.consoleItem = consoleItem;
            this.program = program;
        }

        public void toggle() {
            isVisible = !isVisible;
        }

        public void accept(final char c) {
            inputBuffer.append(c);
        }

        public void handle(final int key, final Consumer<ConsoleQueue.Command> commandConsumer) {
            switch (key) {
                case GLFW_KEY_ENTER:
                    submit(commandConsumer);
                    return;
                case GLFW_KEY_BACKSPACE:
                    backspace();
                    return;
                default:
            }
        }

        private String getInput() {
            return inputBuffer.substring(Definitions.CONSOLE_WELCOME_SYMBOL.length());
        }

        private void backspace() {
            if (inputBuffer.length() > Definitions.CONSOLE_WELCOME_SYMBOL.length())
                inputBuffer.setLength(inputBuffer.length() - 1);
        }

        private void submit(final Consumer<ConsoleQueue.Command> commandConsumer) {
            final var command = new ConsoleQueue.Command(getInput());
            inputBuffer.setLength(Definitions.CONSOLE_WELCOME_SYMBOL.length());

            if (command.isBlank() || command.isComment())
                return;

            commandConsumer.accept(command);
        }

        public void info(final String format, final Object... arguments) {
            print(ConsoleMessageType.Info, format, arguments);
        }

        public void warning(final String format, final Object... arguments) {
            print(ConsoleMessageType.Warning, format, arguments);
        }

        public void error(final String format, final Object... arguments) {
            print(ConsoleMessageType.Error, format, arguments);
        }

        private void print(final ConsoleMessageType type, final String format, final Object... arguments) {
            this.history.add(new ConsoleMessage(type, String.format(format, arguments)));
        }
    }

    public record ConsoleMessage(ConsoleMessageType type, String text) {}

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ConsoleAccessor extends AbstractAccessor {}
}
