package com.venta.engine.core;

import lombok.Getter;

final class FPSCounter {
    private long lastTimeNs = System.nanoTime();
    private int frames = 0;
    private double elapsedSeconds = 0.0;

    @Getter
    private double currentFps = 0.0;

    @Getter
    private double deltaSeconds = 0.0;

    public double tick() {
        final var now = System.nanoTime();
        deltaSeconds = (now - lastTimeNs) / 1_000_000_000.0;
        lastTimeNs = now;

        elapsedSeconds += deltaSeconds;
        frames++;

        if (elapsedSeconds >= 1.0) {
            currentFps = frames / elapsedSeconds;
            frames = 0;
            elapsedSeconds = 0.0;
        }

        return deltaSeconds;
    }
}

