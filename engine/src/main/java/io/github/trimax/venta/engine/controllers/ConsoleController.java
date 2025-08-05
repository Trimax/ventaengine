package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.managers.implementation.ConsoleItemManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.states.ConsoleState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleController extends AbstractController<ConsoleState, Void> {
    private final ConsoleItemManagerImplementation consoleItemManager;
    private final ProgramManagerImplementation programManager;
    private final Memory memory;

    @Override
    protected ConsoleState create(final Void argument) {
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

        return new ConsoleState(consoleItemManager.create(), programManager.load("console"),
                consoleVertexArrayObjectID, consoleVerticesBufferID);
    }

    @Override
    protected void destroy(@NonNull final ConsoleState object) {
        log.debug("Destroying console");

        memory.getVertexArrays().delete(object.getVertexArrayObjectID());
        memory.getBuffers().delete(object.getVerticesBufferID());
    }

    public boolean isVisible() {
        return get().isVisible();
    }

    public void toggle() {
        get().setVisible(!get().isVisible());
    }

    public void accept(final char c) {
        get().getInputBuffer().append(c);
    }

    public void clear() {
        get().getHistory().clear();
    }

    public void handle(final int key, final Consumer<ConsoleCommandQueue.Command> commandConsumer) {
        switch (key) {
            case GLFW_KEY_ENTER:
                submit(commandConsumer);
                return;
            case GLFW_KEY_BACKSPACE:
                backspace();
                return;
            case GLFW_KEY_UP:
                navigateHistory(-1);
                return;
            case GLFW_KEY_DOWN:
                navigateHistory(1);
                return;
            default:
        }
    }

    private String getInput() {
        return get().getInputBuffer().substring(Definitions.CONSOLE_WELCOME_SYMBOL.length());
    }

    private void backspace() {
        if (get().getInputBuffer().length() > Definitions.CONSOLE_WELCOME_SYMBOL.length())
            get().getInputBuffer().setLength(get().getInputBuffer().length() - 1);
    }

    private void submit(final Consumer<ConsoleCommandQueue.Command> commandConsumer) {
        final var command = new ConsoleCommandQueue.Command(getInput());
        get().getInputBuffer().setLength(Definitions.CONSOLE_WELCOME_SYMBOL.length());

        if (command.isBlank() || command.isComment())
            return;

        commandConsumer.accept(command);

        get().getCommands().add(command.value());
        get().setHistoryIndex(get().getCommands().size());
    }

    private void navigateHistory(final int direction) {
        if (get().getHistory().isEmpty())
            return;

        get().setHistoryIndex(get().getHistoryIndex() + direction);

        if (get().getHistoryIndex() < 0)
            get().setHistoryIndex(0);

        if (get().getHistoryIndex() >= get().getCommands().size()) {
            get().setHistoryIndex(get().getCommands().size());
            get().getInputBuffer().setLength(Definitions.CONSOLE_WELCOME_SYMBOL.length());
            return;
        }

        final var historyCommand = get().getCommands().get(get().getHistoryIndex());
        get().getInputBuffer().setLength(Definitions.CONSOLE_WELCOME_SYMBOL.length());
        get().getInputBuffer().append(historyCommand);
    }

    public void header(final String format, final Object... arguments) {
        print(ConsoleMessageType.Header, format, arguments);
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

    public void debug(final String format, final Object... arguments) {
        print(ConsoleMessageType.Debug, format, arguments);
    }

    public void emptyLine() {
        print(ConsoleMessageType.Command, "");
    }

    private void print(final ConsoleMessageType type, final String format, final Object... arguments) {
        this.get().getHistory().add(new ConsoleController.ConsoleMessage(type, String.format(format, arguments)));
    }

    public record ConsoleMessage(ConsoleMessageType type, String text) {}
}
