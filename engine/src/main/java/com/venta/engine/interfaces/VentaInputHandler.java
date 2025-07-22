package com.venta.engine.interfaces;

public interface VentaInputHandler {
    void onKey(int key, int scancode, int action, int mods);

    void onMouseMove(double x, double y);

    void onMouseButton(int button, int action, int mods);
}
