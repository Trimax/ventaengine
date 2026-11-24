package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.shadow.ShadowCascade;
import io.github.trimax.venta.engine.model.common.shadow.ShadowFrame;

final class ShadowCascadeHelperTest {
    private static final float NEAR = 0.1f;
    private static final float FAR = 1000.f;
    private static final int RESOLUTION = 1024;
    private static final float DISTANCE = 300.f;
    private static final Vector3f SUN = new Vector3f(-1.f, -0.6f, -0.4f);

    private static final Matrix4f PROJECTION = new Matrix4f().perspective((float) Math.toRadians(60.0), 16.f / 9.f, NEAR, FAR);

    private final ShadowCascadeHelper calculator = new ShadowCascadeHelper();

    @Test
    void testNearPlaneIsReadFromTheProjection() {
        assertEquals(NEAR, ShadowCascadeHelper.getNear(PROJECTION), 1e-5f, "The near plane of the perspective");
        assertEquals(2.5f, ShadowCascadeHelper.getNear(new Matrix4f().perspective(1.f, 1.f, 2.5f, 80.f)), 1e-5f, "Another near plane");
    }

    @Test
    void testSplitsGrowAndEndAtTheDistance() {
        final var splits = splits(4);

        assertEquals(DISTANCE, splits[3], "The last cascade ends at the shadow distance");

        var previous = NEAR;
        for (final var split : splits) {
            assertTrue(split > previous, "Splits grow: " + split + " after " + previous);
            previous = split;
        }

        assertTrue(splits[1] - splits[0] < splits[3] - splits[2], "Near cascades are shorter than the far ones");
    }

    @Test
    void testOneCascadeCoversTheWholeDistance() {
        assertArrayEquals(new float[]{DISTANCE}, splits(1), "One cascade ends at the distance");
    }

    @Test
    void testSplitsFillOnlyTheirCountOfAnArray() {
        final var splits = new float[]{-1.f, -1.f, -1.f, -1.f, -1.f};

        ShadowCascadeHelper.getSplits(NEAR, DISTANCE, 3, splits);

        assertEquals(DISTANCE, splits[2], "The last split of three");
        assertEquals(-1.f, splits[3], "The rest of the array is not touched");
        assertEquals(-1.f, splits[4], "The rest of the array is not touched");
    }

    @Test
    void testEveryCascadeContainsItsPartOfTheFrustum() {
        final var view = new Matrix4f().lookAt(new Vector3f(10.f, 25.f, 40.f), new Vector3f(60.f, 5.f, -30.f), new Vector3f(0.f, 1.f, 0.f));
        final var frame = frame(view, SUN, 4);

        var start = NEAR;
        for (int i = 0; i < frame.getCount(); i++) {
            final var cascade = frame.getCascade(i);
            for (final var corner : corners(view, start, cascade.getFarDistance())) {
                final var clip = cascade.getMatrix().transform(new Vector4f(corner, 1.f));

                assertTrue(Math.abs(clip.x) <= 1.001f && Math.abs(clip.y) <= 1.001f, "The corner " + corner + " is in the map: " + clip);
                assertTrue(clip.z >= -1.001f && clip.z <= 1.001f, "The corner " + corner + " is between the planes of the sun: " + clip.z);
            }

            start = cascade.getFarDistance();
        }
    }

    @Test
    void testFartherCascadesAreCoarser() {
        final var frame = frame(new Matrix4f(), SUN, 4);

        for (int i = 1; i < frame.getCount(); i++)
            assertTrue(frame.getCascade(i).getTexelSize() > frame.getCascade(i - 1).getTexelSize(), "The texel of the cascade " + i + " is bigger");
    }

    @Test
    void testTexelSizeIsTheWorldSizeOfATexel() {
        final var cascade = frame(new Matrix4f(), SUN, 3).getCascade(0);

        final var origin = cascade.getMatrix().transform(new Vector4f(5.f, 5.f, 5.f, 1.f));
        // Texels have the size of 2 / resolution in the clip space: two points a texel apart along an axis of the sun
        final var right = new Vector3f(1.f, 0.f, 0.f).cross(SUN).normalize();
        final var shifted = cascade.getMatrix().transform(new Vector4f(new Vector3f(5.f, 5.f, 5.f).add(new Vector3f(right).mul(cascade.getTexelSize())), 1.f));

        assertEquals(2.f / RESOLUTION, Math.hypot(shifted.x - origin.x, shifted.y - origin.y), 1e-4f, "A world texel is a texel of the map");
    }

    @Test
    void testShadowsKeepToTheWorldWhenTheCameraMoves() {
        final var point = new Vector4f(33.3f, 4.7f, -12.9f, 1.f);
        final var first = frame(lookAt(new Vector3f(0.f, 10.f, 0.f), new Vector3f(50.f, 0.f, -50.f)), SUN, 4);
        final var second = frame(lookAt(new Vector3f(7.31f, 10.f, -3.17f), new Vector3f(57.31f, 0.f, -53.17f)), SUN, 4);

        for (int i = 0; i < first.getCount(); i++) {
            final var a = texels(first.getCascade(i), point);
            final var b = texels(second.getCascade(i), point);

            assertEquals(0.f, fraction(a[0] - b[0]), 1e-2f, "The point is on the same part of a texel in x, cascade " + i);
            assertEquals(0.f, fraction(a[1] - b[1]), 1e-2f, "The point is on the same part of a texel in y, cascade " + i);
        }
    }

    @Test
    void testTurningTheCameraDoesNotResizeTheMap() {
        final var position = new Vector3f(0.f, 10.f, 0.f);
        final var north = frame(lookAt(position, new Vector3f(0.f, 10.f, -100.f)), SUN, 4);
        final var east = frame(lookAt(position, new Vector3f(100.f, 0.f, 0.f)), SUN, 4);

        for (int i = 0; i < north.getCount(); i++) {
            assertEquals(north.getCascade(i).getTexelSize(), east.getCascade(i).getTexelSize(), 0.f, "The texel of the cascade " + i + " doesn't depend on the direction");
            assertEquals(north.getCascade(i).getMatrix().m00(), east.getCascade(i).getMatrix().m00(), 1e-6f, "The scale of the cascade " + i);
        }
    }

    @Test
    void testSunStraightDownIsHandled() {
        final var frame = frame(new Matrix4f(), new Vector3f(0.f, -1.f, 0.f), 4);

        for (int i = 0; i < frame.getCount(); i++)
            assertTrue(frame.getCascade(i).getMatrix().isFinite(), "A vertical direction doesn't break the matrix");
    }

    @Test
    void testFrameIsFilledAgainWithoutLeftovers() {
        final var viewA = lookAt(new Vector3f(0.f, 10.f, 0.f), new Vector3f(50.f, 0.f, -50.f));
        final var viewB = lookAt(new Vector3f(80.f, 30.f, 20.f), new Vector3f(0.f, 0.f, 0.f));

        // One frame is used for both cameras and a fresh frame is made for the second one: the results are the same
        final var reused = new ShadowFrame(4);
        calculator.calculate(reused, viewA, PROJECTION, SUN, DISTANCE, 4, RESOLUTION);
        calculator.calculate(reused, viewB, PROJECTION, new Vector3f(0.3f, -1.f, 0.2f), 150.f, 3, 512);

        final var fresh = new ShadowFrame(4);
        new ShadowCascadeHelper().calculate(fresh, viewB, PROJECTION, new Vector3f(0.3f, -1.f, 0.2f), 150.f, 3, 512);

        assertEquals(fresh.getCount(), reused.getCount(), "Three cascades after four");
        assertEquals(150.f, reused.getDistance(), "The new distance");
        for (int i = 0; i < fresh.getCount(); i++) {
            assertEquals(fresh.getCascade(i).getFarDistance(), reused.getCascade(i).getFarDistance(), 0.f, "The end of the cascade " + i);
            assertEquals(fresh.getCascade(i).getTexelSize(), reused.getCascade(i).getTexelSize(), 0.f, "The texel of the cascade " + i);
            assertTrue(fresh.getCascade(i).getMatrix().equals(reused.getCascade(i).getMatrix(), 0.f), "The matrix of the cascade " + i);
        }
    }

    @Test
    void testCascadesAreComputedWithoutAllocations() {
        final var view = lookAt(new Vector3f(0.f, 10.f, 0.f), new Vector3f(50.f, 0.f, -50.f));
        final var frame = new ShadowFrame(4);
        final var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

        // The warm up makes the lazy parts of the JVM and of JOML allocate before the measuring
        for (int i = 0; i < 2000; i++)
            calculator.calculate(frame, view, PROJECTION, SUN, DISTANCE, 4, RESOLUTION);

        final var iterations = 2000;
        final var before = threads.getThreadAllocatedBytes(Thread.currentThread().threadId());
        for (int i = 0; i < iterations; i++)
            calculator.calculate(frame, view, PROJECTION, SUN, DISTANCE, 4, RESOLUTION);
        final var allocated = threads.getThreadAllocatedBytes(Thread.currentThread().threadId()) - before;

        // A single object per call would be tens of kilobytes here, the old computation made about 50 of them
        assertTrue(allocated < 16 * 1024, "Computing the cascades allocated " + allocated + " bytes in " + iterations + " calls");
    }

    @Test
    void testInvalidArgumentsAreRejected() {
        final var identity = new Matrix4f();
        final var frame = new ShadowFrame(4);

        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeHelper.getSplits(NEAR, DISTANCE, 0, new float[4]), "No cascades");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeHelper.getSplits(NEAR, NEAR, 4, new float[4]), "The distance isn't behind the near plane");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeHelper.getSplits(0.f, DISTANCE, 4, new float[4]), "The near plane is zero");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeHelper.getSplits(NEAR, DISTANCE, 4, new float[3]), "The array is too small");
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(frame, identity, PROJECTION, new Vector3f(), DISTANCE, 4, RESOLUTION), "No direction");
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(frame, identity, PROJECTION, SUN, DISTANCE, 4, 0), "No texels");
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(frame, identity, PROJECTION, SUN, 0.05f, 4, RESOLUTION), "The distance is before the near plane");
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(frame, identity, PROJECTION, SUN, DISTANCE, 5, RESOLUTION), "The frame has no room for five");
        assertThrows(NullPointerException.class, () -> calculator.calculate(null, identity, PROJECTION, SUN, DISTANCE, 4, RESOLUTION), "No frame");
    }

    private float[] splits(final int count) {
        final var splits = new float[count];
        ShadowCascadeHelper.getSplits(NEAR, DISTANCE, count, splits);

        return splits;
    }

    private ShadowFrame frame(final Matrix4f view, final Vector3f sun, final int count) {
        final var frame = new ShadowFrame(count);
        calculator.calculate(frame, view, PROJECTION, sun, DISTANCE, count, RESOLUTION);

        return frame;
    }

    private static Matrix4f lookAt(final Vector3f eye, final Vector3f target) {
        return new Matrix4f().lookAt(eye, target, new Vector3f(0.f, 1.f, 0.f));
    }

    /** The position of a world point in texels of the map */
    private static float[] texels(final ShadowCascade cascade, final Vector4f point) {
        final var clip = cascade.getMatrix().transform(new Vector4f(point));

        return new float[]{clip.x * 0.5f * RESOLUTION, clip.y * 0.5f * RESOLUTION};
    }

    /** The difference to the nearest whole number */
    private static float fraction(final float value) {
        return value - Math.round(value);
    }

    /** The corners of the part of the frustum between two distances, in the world space */
    private static List<Vector3f> corners(final Matrix4f view, final float start, final float end) {
        final var inverse = new Matrix4f(view).invert();
        final var halfWidth = 1.f / PROJECTION.m00();
        final var halfHeight = 1.f / PROJECTION.m11();

        final var corners = new ArrayList<Vector3f>();
        for (final var depth : new float[]{start, end})
            for (final var x : new float[]{-1.f, 1.f})
                for (final var y : new float[]{-1.f, 1.f})
                    corners.add(inverse.transformPosition(new Vector3f(x * halfWidth * depth, y * halfHeight * depth, -depth)));

        return corners;
    }
}
