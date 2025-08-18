package io.github.trimax.venta.engine.model.states;

import java.util.HashSet;
import java.util.Set;

public final class KeyboardState extends AbstractState {
    private final Set<Integer> pushedButtons = new HashSet<>();

    public void push(final int key) {
        pushedButtons.add(key);
    }

    public void release(final int key) {
        pushedButtons.remove(key);
    }

    public void clear() {
        pushedButtons.clear();
    }

    public boolean isButtonPushed(final int key) {
        return pushedButtons.contains(key);
    }
}
