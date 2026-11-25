package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

final class ObliqueProjectionUtilTest {
    private static Matrix4f perspective() {
        return new Matrix4f().perspective((float) Math.toRadians(60), 1.5f, 0.1f, 1000.f);
    }

    /** Whether the hardware keeps a point of the space of the camera: its clip coordinates are in the box */
    private static boolean isKept(final Matrix4f projection, final float x, final float y, final float z) {
        final var clip = projection.transform(new Vector4f(x, y, z, 1.f));
        return Math.abs(clip.x) <= clip.w && Math.abs(clip.y) <= clip.w && clip.z >= -clip.w && clip.z <= clip.w;
    }

    @Test
    void testPlaneInFrontOfTheCameraCutsWhatIsNearerThanIt() {
        // The plane z = -5 (a point is on its positive side when -z - 5 > 0, that is farther than 5)
        final var projection = ObliqueProjectionUtil.clip(perspective(), new Vector4f(0.f, 0.f, -1.f, -5.f));

        assertFalse(isKept(projection, 0.f, 0.f, -3.f), "Nearer than the plane");
        assertTrue(isKept(projection, 0.f, 0.f, -8.f), "Farther than the plane");
        assertTrue(isKept(projection, 0.f, 0.f, -400.f), "Far away is kept");
    }

    @Test
    void testTiltedPlaneCutsAlongItself() {
        // The plane through (0, 0, -10) that leans: a point is kept when it is on the side the normal looks at
        final var normal = new Vector4f(0.f, 1.f, 0.f, 0.f).add(0.f, 0.f, -0.5f, 0.f).normalize3();
        final var plane = new Vector4f(normal.x, normal.y, normal.z, -(normal.z * -10.f));
        final var projection = ObliqueProjectionUtil.clip(perspective(), plane);

        assertTrue(isKept(projection, 0.f, 4.f, -10.f), "Over the plane");
        assertFalse(isKept(projection, 0.f, -4.f, -10.f), "Under the plane");
        assertTrue(isKept(projection, 0.f, 2.f, -30.f));
    }

    @Test
    void testPointOnThePlaneIsOnTheNearPlane() {
        final var projection = ObliqueProjectionUtil.clip(perspective(), new Vector4f(0.f, 0.f, -1.f, -5.f));

        final var clip = projection.transform(new Vector4f(0.f, 0.f, -5.f, 1.f));
        assertEquals(-1.f, clip.z / clip.w, 1e-4f, "The depth on the plane is the depth of the near plane");
    }

    @Test
    void testOnlyTheDepthIsChanged() {
        final var original = perspective();
        final var projection = ObliqueProjectionUtil.clip(perspective(), new Vector4f(0.f, 0.f, -1.f, -5.f));

        final var point = new Vector4f(3.f, 2.f, -20.f, 1.f);
        final var before = original.transform(new Vector4f(point));
        final var after = projection.transform(new Vector4f(point));

        assertEquals(before.x / before.w, after.x / after.w, 1e-5f, "The point is where it was on the screen");
        assertEquals(before.y / before.w, after.y / after.w, 1e-5f);
    }
}
