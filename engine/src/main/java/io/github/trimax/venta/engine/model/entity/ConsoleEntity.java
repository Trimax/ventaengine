package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.memory.MemoryBlock;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;

@Getter
public final class ConsoleEntity extends AbstractEntity implements ConsoleView {
    private final StringBuilder inputBuffer = new StringBuilder(Definitions.CONSOLE_WELCOME_SYMBOL);
    private final List<ConsoleManagerImplementation.ConsoleMessage> history = new ArrayList<>();

    private final ConsoleItemEntity consoleItem;
    private final ProgramEntity program;

    private final MemoryBlock<Integer> vertexArrayObject;
    private final MemoryBlock<Integer> verticesBuffer;

    private boolean isVisible;

    public ConsoleEntity(@NonNull final String name,
                         @NonNull final ConsoleItemEntity consoleItem,
                         @NonNull final ProgramEntity program,
                         @NonNull final MemoryBlock<Integer> vertexArrayObject,
                         @NonNull final MemoryBlock<Integer> verticesBuffer) {
        super(name);

        this.vertexArrayObject = vertexArrayObject;
        this.verticesBuffer = verticesBuffer;

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
        this.history.add(new ConsoleManagerImplementation.ConsoleMessage(type, String.format(format, arguments)));
    }
}
