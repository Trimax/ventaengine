package io.github.trimax.venta.engine.model.instance.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

/** Rotates the camera basis like CameraInstanceImplementation does and checks the computed angles */
final class CameraAnglesTest {
    private static final float DELTA = 1e-5f;

    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);

    @Test
    void testDefaultOrientationHasNoRotation() {
        assertAngles(0, 0, 0);
    }

    @Test
    void testYaw() {
        rotate(up, 0.5f);
        assertAngles(0, 0.5f, 0);
    }

    @Test
    void testPitch() {
        rotate(right, 0.3f);
        assertAngles(0.3f, 0, 0);
    }

    @Test
    void testRoll() {
        rotate(front, 0.2f);
        assertAngles(0, 0, 0.2f);
    }

    @Test
    void testYawThenPitch() {
        rotate(up, -1.0f);
        rotate(right, 0.4f);
        assertAngles(0.4f, -1.0f, 0);
    }

    @Test
    void testLookingStraightDown() {
        rotate(up, 0.7f);
        rotate(right, (float) -Math.PI / 2);

        final var angles = angles();
        assertEquals(-Math.PI / 2, angles.x, 1e-3, "pitch");
        assertEquals(0.7f, angles.y, 1e-3, "yaw is taken from the right vector");
        assertFalse(Float.isNaN(angles.z), "roll must not be NaN");
    }

    private void rotate(final Vector3fc axis, final float radians) {
        final var rotation = new Matrix3f().rotate(radians, new Vector3f(axis));
        front.mul(rotation).normalize();
        up.mul(rotation).normalize();
        right.mul(rotation).normalize();
    }

    private Vector3f angles() {
        return CameraInstanceImplementation.anglesOf(front, up, right);
    }

    private void assertAngles(final float pitch, final float yaw, final float roll) {
        final var angles = angles();
        assertEquals(pitch, angles.x, DELTA, "pitch");
        assertEquals(yaw, angles.y, DELTA, "yaw");
        assertEquals(roll, angles.z, DELTA, "roll");
    }
}
