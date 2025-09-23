package io.github.trimax.venta.engine.model.states;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.definitions.DefinitionsConsole;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class ConsoleState extends AbstractState {
    private final StringBuilder inputBuffer = new StringBuilder(DefinitionsConsole.CONSOLE_WELCOME_SYMBOL);
    private final List<ConsoleController.ConsoleMessage> history = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();

    private final ProgramEntityImplementation program;
    private final Geometry geometry;

    private int historyIndex = -1;

    @Setter
    private boolean isVisible;

    public String getBuffer() {
        return inputBuffer.toString();
    }

    public void accept(final char c) {
        inputBuffer.append(c);
    }

    public void clear() {
        history.clear();
    }

    public void eraseLast() {
        if (inputBuffer.length() > DefinitionsConsole.CONSOLE_WELCOME_SYMBOL.length())
            inputBuffer.setLength(inputBuffer.length() - 1);
    }

    public void navigateHistory(final int direction) {
        if (history.isEmpty())
            return;

        historyIndex += direction;

        if (historyIndex < 0)
            historyIndex = 0;

        if (historyIndex >= commands.size()) {
            historyIndex = commands.size();
            inputBuffer.setLength(DefinitionsConsole.CONSOLE_WELCOME_SYMBOL.length());
            return;
        }

        final var historyCommand = commands.get(historyIndex);
        inputBuffer.setLength(DefinitionsConsole.CONSOLE_WELCOME_SYMBOL.length());
        inputBuffer.append(historyCommand);
    }

    public void submit(final Consumer<ConsoleCommandQueue.Command> commandConsumer) {
        final var command = new ConsoleCommandQueue.Command(getInput());
        inputBuffer.setLength(DefinitionsConsole.CONSOLE_WELCOME_SYMBOL.length());

        if (command.isBlank() || command.isComment())
            return;

        commandConsumer.accept(command);

        commands.add(command.value());
        historyIndex = commands.size();
    }

    private String getInput() {
        return inputBuffer.substring(DefinitionsConsole.CONSOLE_WELCOME_SYMBOL.length());
    }
}
