package com.venta.engine.interfaces;

public interface VentaEngineInputHandler {
    VentaEngineInputHandler DEFAULT = new VentaEngineInputHandler() {};

    default void onKey(final int key, final int scancode, final int action, final int mods) {}

    default void onMouseMove(final double x, final double y) {}

    default void onMouseButton(final int button, final int action, final int mods) {}
}
