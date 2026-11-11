package io.github.trimax.venta.mocks.set8;

import java.util.ArrayList;
import java.util.List;

/** Records what the container did, so a test can check it after the application has finished */
public final class Events {
    public static final List<String> PLUGINS = new ArrayList<>();
    public static final List<String> CLOSED = new ArrayList<>();

    private Events() {
    }

    public static void reset() {
        PLUGINS.clear();
        CLOSED.clear();
    }
}
