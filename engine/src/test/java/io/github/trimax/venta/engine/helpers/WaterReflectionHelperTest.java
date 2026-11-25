package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

final class WaterReflectionHelperTest {
    private static final float EPSILON = 1e-4f;

    private final WaterReflectionHelper helper = new WaterReflectionHelper();
    private final Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(60), 1.5f, 0.1f, 1000.f);

    private void update(final Vector3f position, final Vector3f front, final float height) {
        final var normalized = new Vector3f(front).normalize();
        final var right = new Vector3f(normalized).cross(0.f, 1.f, 0.f).normalize();
        final var up = new Vector3f(right).cross(normalized).normalize();

        helper.update(position, normalized, up, projection, height);
    }

    private boolean isKept(final float x, final float y, final float z) {
        final var clip = helper.getViewProjectionMatrix().transform(new Vector4f(x, y, z, 1.f));
        return Math.abs(clip.x) <= clip.w && Math.abs(clip.y) <= clip.w && clip.z >= -clip.w && clip.z <= clip.w;
    }

    @Test
    void testCameraIsMirroredInThePlane() {
        update(new Vector3f(1.f, 5.f, 2.f), new Vector3f(0.f, -0.3f, -1.f), 2.f);

        assertEquals(1.f, helper.getPosition().x, EPSILON);
        assertEquals(-1.f, helper.getPosition().y, EPSILON, "As far under the plane as it was over it");
        assertEquals(2.f, helper.getPosition().z, EPSILON);
    }

    @Test
    void testCameraAtTheHeightOfThePlaneStaysThere() {
        update(new Vector3f(0.f, 3.f, 0.f), new Vector3f(0.f, 0.f, -1.f), 3.f);

        assertEquals(3.f, helper.getPosition().y, EPSILON);
    }

    @Test
    void testMirrorImageLooksTheOtherWayUpOrDown() {
        // The camera looks down at the water: its mirror image looks up at it
        update(new Vector3f(0.f, 6.f, 0.f), new Vector3f(0.f, -0.3f, -1.f), 0.f);

        final var forward = new Vector3f(0.f, 0.f, -1.f);
        helper.getViewMatrix().invert(new Matrix4f()).transformDirection(forward);
        assertTrue(forward.y > 0.f, "The mirror image looks up: " + forward.y);
        assertEquals(-1.f * 0.958f, forward.z, 0.01f, "And on, the same way along the plane");
    }

    @Test
    void testWhatIsAboveTheWaterIsKeptAndWhatIsUnderItIsCut() {
        update(new Vector3f(0.f, 6.f, 0.f), new Vector3f(0.f, -0.3f, -1.f), 0.f);

        assertTrue(isKept(0.f, 3.f, -20.f), "Something above the water in front is in the reflection");
        assertTrue(isKept(2.f, 0.5f, -15.f), "Just above the plane");
        assertFalse(isKept(0.f, -3.f, -20.f), "Something under the water is not");
        assertFalse(isKept(0.f, -0.5f, -15.f), "Just under the plane");
    }

    @Test
    void testPlaneAtAnotherHeight() {
        update(new Vector3f(10.f, 20.f, 10.f), new Vector3f(0.f, -0.5f, -1.f), 12.f);

        assertTrue(isKept(10.f, 15.f, -25.f), "Above the plane at 12");
        assertFalse(isKept(10.f, 9.f, -25.f), "Under it");
    }

    @Test
    void testUpdateRepeatedGivesTheSameMatrices() {
        update(new Vector3f(0.f, 6.f, 0.f), new Vector3f(0.f, -0.3f, -1.f), 0.f);
        final var first = new Matrix4f(helper.getViewProjectionMatrix());

        update(new Vector3f(50.f, 9.f, 10.f), new Vector3f(1.f, -0.1f, 0.f), 3.f);
        update(new Vector3f(0.f, 6.f, 0.f), new Vector3f(0.f, -0.3f, -1.f), 0.f);

        assertTrue(first.equals(helper.getViewProjectionMatrix(), 1e-5f), "Nothing is left of the previous frame");
    }
}
