package io.github.trimax.venta.engine.core;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Console {
    private final List<String> history = new ArrayList<>();
    private final StringBuilder inputBuffer = new StringBuilder();
    private boolean visible;

    public void toggle() {
        visible = !visible;
    }

    public void accept(final char c) {
        inputBuffer.append(c);
    }

    public void backspace() {
        if (!inputBuffer.isEmpty())
            inputBuffer.setLength(inputBuffer.length() - 1);
    }

    public void submit() {
        final var command = inputBuffer.toString();
        history.add(command);

        execute(command);
        inputBuffer.setLength(0);
    }

    private void execute(final String command) {
        log.info("Command for execution: {}", command);
    }

    public void render(final int screenWidth, final int screenHeight) {
        if (!visible)
            return;

        history.size();
    }
}
