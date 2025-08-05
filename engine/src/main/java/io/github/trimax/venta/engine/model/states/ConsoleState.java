package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.model.entity.ConsoleItemEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class ConsoleState extends AbstractState {
    private final StringBuilder inputBuffer = new StringBuilder(Definitions.CONSOLE_WELCOME_SYMBOL);
    private final List<ConsoleController.ConsoleMessage> history = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();

    private final ConsoleItemEntity consoleItem;
    private final ProgramEntity program;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;

    private int historyIndex = -1;
    private boolean isVisible;
}
