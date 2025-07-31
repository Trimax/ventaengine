package io.github.trimax.venta.engine.core;

import java.util.ArrayList;
import java.util.List;

public final class Console {
    private final List<String> history = new ArrayList<>();
    private final List<String> output = new ArrayList<>();
    private StringBuilder inputBuffer = new StringBuilder();

    private int historyIndex = -1;
    private boolean visible = false;

    public void toggle() {
        visible = !visible;
    }

    public void inputChar(char c) {
        inputBuffer.append(c);
    }

    public void backspace() {
        if (!inputBuffer.isEmpty())
            inputBuffer.setLength(inputBuffer.length() - 1);
    }

    public void submit() {
        String command = inputBuffer.toString();
        history.add(command);
        output.add("> " + command);
        execute(command);
        inputBuffer.setLength(0);
        historyIndex = history.size();
    }

    public void scrollHistory(int delta) {
        historyIndex = Math.max(0, Math.min(history.size() - 1, historyIndex + delta));
        inputBuffer.setLength(0);
        inputBuffer.append(history.get(historyIndex));
    }

    private void execute(String command) {
        // Простейший парсер
        if (command.equals("clear")) {
            output.clear();
        } else if (command.equals("help")) {
            output.add("Available commands: help, clear");
        } else {
            output.add("Unknown command: " + command);
        }
    }

    public void render(int screenWidth, int screenHeight) {
        if (!visible) return;

    }
}
