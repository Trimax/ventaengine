package io.github.trimax.venta.engine.console;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class Console implements ConsoleView {
    private final StringBuilder inputBuffer = new StringBuilder();
    private final List<String> history = new ArrayList<>();

    @Setter
    private int vertexBufferID;
    private boolean visible;

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

    @Override
    public String getID() {
        return "console";
    }

    @Override
    public String getName() {
        return "console";
    }
}
