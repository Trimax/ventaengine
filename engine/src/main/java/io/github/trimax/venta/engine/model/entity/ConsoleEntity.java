package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

@Getter
public final class ConsoleEntity extends AbstractEntity implements ConsoleView {
    private final StringBuilder inputBuffer = new StringBuilder(Definitions.CONSOLE_WELCOME_SYMBOL);
    private final List<ConsoleManagerImplementation.ConsoleMessage> history = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();

    private final ConsoleItemEntity consoleItem;
    private final ProgramEntity program;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;

    private int historyIndex = -1;
    private boolean isVisible;

    public ConsoleEntity(final String name,
                         @NonNull final ConsoleItemEntity consoleItem,
                         @NonNull final ProgramEntity program,
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

        commands.add(command.value());
        historyIndex = commands.size();
    }

    private void navigateHistory(final int direction) {
        if (history.isEmpty())
            return;

        historyIndex += direction;

        if (historyIndex < 0)
            historyIndex = 0;

        if (historyIndex >= commands.size()) {
            historyIndex = commands.size();
            inputBuffer.setLength(Definitions.CONSOLE_WELCOME_SYMBOL.length());
            return;
        }

        final var historyCommand = commands.get(historyIndex);
        inputBuffer.setLength(Definitions.CONSOLE_WELCOME_SYMBOL.length());
        inputBuffer.append(historyCommand);
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
        this.history.add(new ConsoleManagerImplementation.ConsoleMessage(type, String.format(format, arguments)));
    }
}
