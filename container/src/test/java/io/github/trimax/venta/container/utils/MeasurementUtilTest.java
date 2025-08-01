package io.github.trimax.venta.container.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

final class MeasurementUtilTest {
    @Test
    void testMeasureRunnable() {
        final var flag = new AtomicBoolean(false);

        MeasurementUtil.measure("Test", () -> flag.set(true));
        assertTrue(flag.get(), "Measurement util should execute the provided runnable");
    }

    @Test
    void testMeasureSupplier() {
        final var flag = new AtomicBoolean(false);

        final var expectedValue = 42;
        final var result = MeasurementUtil.measure("Test", () -> {
            flag.set(true);
            return expectedValue;
        });

        assertTrue(flag.get(), "Measurement util should execute the provided supplier");
        assertEquals(expectedValue, result, "Measurement util should return expected value from supplier");
    }

    @Test
    void testMeasureWithException() {
        assertThrows(RuntimeException.class, () -> {
            MeasurementUtil.measure("Test", () -> {
                throw new RuntimeException("Test exception");
            });
        }, "Measurement util should throw an exception when the runnable throws one");
    }
}
