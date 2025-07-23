package com.venta.engine.model.core;

import lombok.Getter;

@Getter
public final class Counter {
    private int value = 0;

    public int getAndIncrement() {
        return value++;
    }
}
