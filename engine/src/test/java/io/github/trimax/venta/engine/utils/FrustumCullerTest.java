package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;

/**
 * The view is a camera at the origin that looks along -z (a perspective of 90 degrees, from 1 to 100), or a box of the
 * sun that is 20 wide (x and y from -10 to 10, z from -10 to 10).
 */
final class FrustumCullerTest {
    private static final Matrix4f IDENTITY = new Matrix4f();

    private static Matrix4f camera() {
        return new Matrix4f().perspective((float) Math.toRadians(90), 1.f, 1.f, 100.f);
    }

    private static Matrix4f sun() {
        return new Matrix4f().ortho(-10.f, 10.f, -10.f, 10.f, -10.f, 10.f);
    }

    /** A box of 2 units around a point */
    private static BoundingBox around(final float x, final float y, final float z) {
        return new BoundingBox(new Vector3f(x - 1, y - 1, z - 1), new Vector3f(x + 1, y + 1, z + 1));
    }

    private static FrustumCuller culler(final Matrix4f view, final int planes) {
        final var culler = new FrustumCuller();
        culler.set(view, planes);
        return culler;
    }

    @Test
    void testWhatIsInFrontOfTheCameraIsVisible() {
        assertTrue(culler(camera(), FrustumCuller.ALL_PLANES).isVisible(around(0, 0, -20), IDENTITY, 0));
    }

    @Test
    void testWhatIsBesideBehindOrBeyondTheCameraIsCulled() {
        final var culler = culler(camera(), FrustumCuller.ALL_PLANES);

        assertFalse(culler.isVisible(around(60, 0, -20), IDENTITY, 0), "Right of the view");
        assertFalse(culler.isVisible(around(-60, 0, -20), IDENTITY, 0), "Left of the view");
        assertFalse(culler.isVisible(around(0, 60, -20), IDENTITY, 0), "Above the view");
        assertFalse(culler.isVisible(around(0, -60, -20), IDENTITY, 0), "Below the view");
        assertFalse(culler.isVisible(around(0, 0, 20), IDENTITY, 0), "Behind the camera");
        assertFalse(culler.isVisible(around(0, 0, -300), IDENTITY, 0), "Beyond the far plane");
    }

    @Test
    void testWhatCrossesTheEdgeOfTheViewIsVisible() {
        assertTrue(culler(camera(), FrustumCuller.ALL_PLANES).isVisible(around(21, 0, -20), IDENTITY, 0), "A part of the box is in the view");
    }

    @Test
    void testTheMatrixOfTheObjectMovesTurnsAndScalesTheBox() {
        final var culler = culler(camera(), FrustumCuller.ALL_PLANES);
        final var box = around(0, 0, 0);

        assertTrue(culler.isVisible(box, new Matrix4f().translation(0, 0, -20), 0));
        assertFalse(culler.isVisible(box, new Matrix4f().translation(60, 0, -20), 0));
        assertTrue(culler.isVisible(box, new Matrix4f().translation(60, 0, -20).scale(30), 0), "Scaled it reaches into the view");
        assertTrue(culler.isVisible(new BoundingBox(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 80)),
                new Matrix4f().translation(0, 0, -20).rotateY((float) Math.toRadians(90)), 0), "Turned a quarter its long side lies along x");
    }

    @Test
    void testTheMarginKeepsWhatReachesIntoTheView() {
        final var culler = culler(camera(), FrustumCuller.ALL_PLANES);
        final var box = around(24, 0, -20);

        assertFalse(culler.isVisible(box, IDENTITY, 0), "Just outside");
        assertTrue(culler.isVisible(box, IDENTITY, 4), "A tree that sways in the wind can reach in");
    }

    @Test
    void testBoxWithoutPointsIsNeverCulled() {
        final var empty = new BoundingBox(new Vector3f(Float.POSITIVE_INFINITY), new Vector3f(Float.NEGATIVE_INFINITY));

        assertTrue(culler(camera(), FrustumCuller.ALL_PLANES).isVisible(empty, IDENTITY, 0));
        assertTrue(culler(camera(), FrustumCuller.ALL_PLANES).isVisible(empty, new Matrix4f().translation(1000, 0, 0), 0));
    }

    @Test
    void testTheSunSeesTheSidesOnlyWhenAskedTo() {
        final var all = culler(sun(), FrustumCuller.ALL_PLANES);
        final var sides = culler(sun(), FrustumCuller.SIDE_PLANES);

        assertTrue(all.isVisible(around(0, 0, 0), IDENTITY, 0));
        assertTrue(sides.isVisible(around(0, 0, 0), IDENTITY, 0));

        assertFalse(all.isVisible(around(40, 0, 0), IDENTITY, 0), "Beside the box of the sun");
        assertFalse(sides.isVisible(around(40, 0, 0), IDENTITY, 0));
        assertFalse(sides.isVisible(around(0, 40, 0), IDENTITY, 0));

        // Nearer to the sun than the map and farther from it: the depth is clamped, so only the sides decide
        assertFalse(all.isVisible(around(0, 0, 40), IDENTITY, 0));
        assertTrue(sides.isVisible(around(0, 0, 40), IDENTITY, 0), "It still casts its shadow into the map");
        assertTrue(sides.isVisible(around(0, 0, -40), IDENTITY, 0));
    }

    @Test
    void testTheViewCanBeChangedBetweenTests() {
        final var culler = new FrustumCuller();
        final var box = around(0, 0, -20);

        culler.set(camera(), FrustumCuller.ALL_PLANES);
        assertTrue(culler.isVisible(box, IDENTITY, 0));

        culler.set(new Matrix4f(camera()).translate(0, 0, 200), FrustumCuller.ALL_PLANES);
        assertFalse(culler.isVisible(box, IDENTITY, 0), "The camera moved away");
    }

    @Test
    void testArgumentsAreRequired() {
        final var culler = new FrustumCuller();

        assertThrows(NullPointerException.class, () -> culler.set(null, FrustumCuller.ALL_PLANES));
        assertThrows(NullPointerException.class, () -> culler.isVisible(null, IDENTITY, 0));
        assertThrows(NullPointerException.class, () -> culler.isVisible(around(0, 0, 0), null, 0));
    }
}
