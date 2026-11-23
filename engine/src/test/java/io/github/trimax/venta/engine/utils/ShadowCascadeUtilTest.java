package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.shadow.ShadowCascade;

final class ShadowCascadeUtilTest {
    private static final float NEAR = 0.1f;
    private static final float FAR = 1000.f;
    private static final int RESOLUTION = 1024;
    private static final float DISTANCE = 300.f;
    private static final Vector3f SUN = new Vector3f(-1.f, -0.6f, -0.4f);

    private static final Matrix4f PROJECTION = new Matrix4f().perspective((float) Math.toRadians(60.0), 16.f / 9.f, NEAR, FAR);

    @Test
    void testNearPlaneIsReadFromTheProjection() {
        assertEquals(NEAR, ShadowCascadeUtil.getNear(PROJECTION), 1e-5f, "The near plane of the perspective");
        assertEquals(2.5f, ShadowCascadeUtil.getNear(new Matrix4f().perspective(1.f, 1.f, 2.5f, 80.f)), 1e-5f, "Another near plane");
    }

    @Test
    void testSplitsGrowAndEndAtTheDistance() {
        final var splits = ShadowCascadeUtil.getSplits(NEAR, DISTANCE, 4);

        assertEquals(4, splits.length, "A split per cascade");
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
        assertArrayEquals(new float[]{DISTANCE}, ShadowCascadeUtil.getSplits(NEAR, DISTANCE, 1), "One cascade ends at the distance");
    }

    @Test
    void testEveryCascadeContainsItsPartOfTheFrustum() {
        final var view = new Matrix4f().lookAt(new Vector3f(10.f, 25.f, 40.f), new Vector3f(60.f, 5.f, -30.f), new Vector3f(0.f, 1.f, 0.f));
        final var cascades = ShadowCascadeUtil.getCascades(view, PROJECTION, SUN, DISTANCE, 4, RESOLUTION);

        var start = NEAR;
        for (final var cascade : cascades) {
            for (final var corner : corners(view, start, cascade.farDistance())) {
                final var clip = cascade.matrix().transform(new Vector4f(corner, 1.f));

                assertTrue(Math.abs(clip.x) <= 1.001f && Math.abs(clip.y) <= 1.001f, "The corner " + corner + " is in the map: " + clip);
                assertTrue(clip.z >= -1.001f && clip.z <= 1.001f, "The corner " + corner + " is between the planes of the sun: " + clip.z);
            }

            start = cascade.farDistance();
        }
    }

    @Test
    void testFartherCascadesAreCoarser() {
        final var cascades = ShadowCascadeUtil.getCascades(new Matrix4f(), PROJECTION, SUN, DISTANCE, 4, RESOLUTION);

        for (int i = 1; i < cascades.size(); i++)
            assertTrue(cascades.get(i).texelSize() > cascades.get(i - 1).texelSize(), "The texel of the cascade " + i + " is bigger");
    }

    @Test
    void testTexelSizeIsTheWorldSizeOfATexel() {
        final var cascade = ShadowCascadeUtil.getCascades(new Matrix4f(), PROJECTION, SUN, DISTANCE, 3, RESOLUTION).getFirst();

        final var origin = cascade.matrix().transform(new Vector4f(5.f, 5.f, 5.f, 1.f));
        // Texels have the size of 2 / resolution in the clip space: two points a texel apart along an axis of the sun
        final var right = new Vector3f(1.f, 0.f, 0.f).cross(SUN).normalize();
        final var shifted = cascade.matrix().transform(new Vector4f(new Vector3f(5.f, 5.f, 5.f).add(new Vector3f(right).mul(cascade.texelSize())), 1.f));

        assertEquals(2.f / RESOLUTION, Math.hypot(shifted.x - origin.x, shifted.y - origin.y), 1e-4f, "A world texel is a texel of the map");
    }

    @Test
    void testShadowsKeepToTheWorldWhenTheCameraMoves() {
        final var point = new Vector4f(33.3f, 4.7f, -12.9f, 1.f);
        final var first = lookAt(new Vector3f(0.f, 10.f, 0.f), new Vector3f(50.f, 0.f, -50.f));
        final var second = lookAt(new Vector3f(7.31f, 10.f, -3.17f), new Vector3f(57.31f, 0.f, -53.17f));

        final var firstCascades = ShadowCascadeUtil.getCascades(first, PROJECTION, SUN, DISTANCE, 4, RESOLUTION);
        final var secondCascades = ShadowCascadeUtil.getCascades(second, PROJECTION, SUN, DISTANCE, 4, RESOLUTION);

        for (int i = 0; i < firstCascades.size(); i++) {
            final var a = texels(firstCascades.get(i), point);
            final var b = texels(secondCascades.get(i), point);

            assertEquals(0.f, fraction(a[0] - b[0]), 1e-2f, "The point is on the same part of a texel in x, cascade " + i);
            assertEquals(0.f, fraction(a[1] - b[1]), 1e-2f, "The point is on the same part of a texel in y, cascade " + i);
        }
    }

    @Test
    void testTurningTheCameraDoesNotResizeTheMap() {
        final var position = new Vector3f(0.f, 10.f, 0.f);
        final var north = ShadowCascadeUtil.getCascades(lookAt(position, new Vector3f(0.f, 10.f, -100.f)), PROJECTION, SUN, DISTANCE, 4, RESOLUTION);
        final var east = ShadowCascadeUtil.getCascades(lookAt(position, new Vector3f(100.f, 0.f, 0.f)), PROJECTION, SUN, DISTANCE, 4, RESOLUTION);

        for (int i = 0; i < north.size(); i++) {
            assertEquals(north.get(i).texelSize(), east.get(i).texelSize(), 0.f, "The texel of the cascade " + i + " doesn't depend on the direction");
            assertEquals(north.get(i).matrix().m00(), east.get(i).matrix().m00(), 1e-6f, "The scale of the cascade " + i);
        }
    }

    @Test
    void testSunStraightDownIsHandled() {
        final var cascades = ShadowCascadeUtil.getCascades(new Matrix4f(), PROJECTION, new Vector3f(0.f, -1.f, 0.f), DISTANCE, 4, RESOLUTION);

        for (final ShadowCascade cascade : cascades)
            assertTrue(cascade.matrix().isFinite(), "A vertical direction doesn't break the matrix");
    }

    @Test
    void testInvalidArgumentsAreRejected() {
        final var identity = new Matrix4f();

        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeUtil.getSplits(NEAR, DISTANCE, 0), "No cascades");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeUtil.getSplits(NEAR, NEAR, 4), "The distance isn't behind the near plane");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeUtil.getSplits(0.f, DISTANCE, 4), "The near plane is zero");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeUtil.getCascades(identity, PROJECTION, new Vector3f(), DISTANCE, 4, RESOLUTION), "No direction");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeUtil.getCascades(identity, PROJECTION, SUN, DISTANCE, 4, 0), "No texels");
        assertThrows(IllegalArgumentException.class, () -> ShadowCascadeUtil.getCascades(identity, PROJECTION, SUN, 0.05f, 4, RESOLUTION), "The distance is before the near plane");
    }

    private static Matrix4f lookAt(final Vector3f eye, final Vector3f target) {
        return new Matrix4f().lookAt(eye, target, new Vector3f(0.f, 1.f, 0.f));
    }

    /** The position of a world point in texels of the map */
    private static float[] texels(final ShadowCascade cascade, final Vector4f point) {
        final var clip = cascade.matrix().transform(new Vector4f(point));

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
