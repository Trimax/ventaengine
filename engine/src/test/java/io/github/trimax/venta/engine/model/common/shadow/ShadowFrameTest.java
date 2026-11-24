package io.github.trimax.venta.engine.model.common.shadow;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

final class ShadowFrameTest {
    @Test
    void testFrameIsNotActiveUntilItIsFinished() {
        final var frame = new ShadowFrame(4);

        assertFalse(frame.isActive(), "A new frame has no shadows");
        assertEquals(0, frame.getCount());
        assertEquals(4, frame.getCapacity());
    }

    @Test
    void testFrameKeepsWhatItWasGiven() {
        final var frame = new ShadowFrame(4);
        final var forward = new Vector3f(0.f, 0.f, -1.f);

        frame.begin(3, 250.f);
        frame.getCascade(0).set(10.f, 0.5f);
        frame.activate(forward);
        forward.set(1.f, 0.f, 0.f);

        assertTrue(frame.isActive());
        assertEquals(3, frame.getCount());
        assertEquals(250.f, frame.getDistance());
        assertEquals(10.f, frame.getCascade(0).getFarDistance());
        assertEquals(0.5f, frame.getCascade(0).getTexelSize());
        assertEquals(new Vector3f(0.f, 0.f, -1.f), frame.getCameraForward(), "The frame has its own copy of the direction");
    }

    @Test
    void testFrameCanBeDeactivatedAndUsedAgain() {
        final var frame = new ShadowFrame(2);
        frame.begin(2, 100.f);
        frame.activate(new Vector3f(1.f, 0.f, 0.f));

        frame.deactivate();

        assertFalse(frame.isActive());
        assertEquals(0, frame.getCount(), "Nothing is left of the last frame");

        frame.begin(1, 50.f);
        frame.activate(new Vector3f(0.f, 1.f, 0.f));
        assertTrue(frame.isActive());
        assertEquals(1, frame.getCount());
    }

    @Test
    void testCascadesAreMadeOnceAndKept() {
        final var frame = new ShadowFrame(4);

        assertSame(frame.getCascade(2), frame.getCascade(2), "The same holder every time");
        assertNotSame(frame.getCascade(0), frame.getCascade(1), "A holder per cascade");
    }

    @Test
    void testInvalidArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ShadowFrame(0), "No room");

        final var frame = new ShadowFrame(2);
        assertThrows(IllegalArgumentException.class, () -> frame.begin(0, 10.f), "No cascades");
        assertThrows(IllegalArgumentException.class, () -> frame.begin(3, 10.f), "More than the room");
        assertThrows(NullPointerException.class, () -> frame.activate(null), "No direction");
    }
}
