package com.venta.engine.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
@UtilityClass
public final class MeasurementUtil {
    public void measure(final String name, final Runnable runnable) {
        measure(name, () -> {
            runnable.run();
            return null;
        });
    }

    public <O> O measure(final String name, final Supplier<O> supplier) {
        final long timeStarted = System.currentTimeMillis();
        final var object = supplier.get();
        final long timeFinished = System.currentTimeMillis();

        log.info("{} took {}ms", name, timeFinished - timeStarted);
        return object;
    }
}
