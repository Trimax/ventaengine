package com.venta.examples.cube;

import com.venta.engine.interfaces.VentaInputHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class InputHandler implements VentaInputHandler {
    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        log.info("Key: {}, Action: {}, Mods: {}", key, action, mods);
    }

    @Override
    public void onMouseMove(final double x, final double y) {
        log.info("Mouse: {} x {}", x, y);
    }

    @Override
    public void onMouseButton(final int button, final int action, final int mods) {
        log.info("Mouse button: {} action {} mods {}", button, action, mods);
    }
}
