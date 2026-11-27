package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoDrag;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoHandle;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;

final class GizmoUtilTest {
    private static final float EPSILON = 1e-3f;
    private static final Vector3f ORIGIN = new Vector3f();
    private static final Matrix3f WORLD = new Matrix3f();

    /** The ray from the camera to a point */
    private static Ray toward(final Vector3f camera, final float x, final float y, final float z) {
        return new Ray(new Vector3f(camera), new Vector3f(x, y, z).sub(camera).normalize());
    }

    private static void assertVector(final Vector3f expected, final Vector3f actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }

    private static float size(final Vector3f camera) {
        return GizmoUtil.size(ORIGIN, camera);
    }

    @Test
    void testHandlesAreAsBigOnTheScreenAtAnyDistance() {
        assertEquals(1.4f, size(new Vector3f(0.f, 0.f, 10.f)), EPSILON);
        assertEquals(14.f, size(new Vector3f(0.f, 0.f, 100.f)), EPSILON);
        assertEquals(GizmoUtil.MINIMAL_SIZE, size(new Vector3f(0.f, 0.f, 0.1f)), EPSILON, "Not smaller than the least size");
    }

    @Test
    void testArrowIsTakenWhereTheRayGoesNearIt() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);

        assertEquals(GizmoHandle.AxisX, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera));
        assertEquals(GizmoHandle.AxisY, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 0.f, 0.8f, 0.f), camera));
        assertEquals(GizmoHandle.AxisZ, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 0.f, 0.f, 0.f), camera), "An arrow that points at the camera");
    }

    @Test
    void testRayThatGoesAwayFromTheHandlesTakesNone() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);

        assertNull(GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 5.f, 5.f, 0.f), camera));
        assertNull(GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 1.f, 0.6f, 0.f), camera), "Between two arrows");
    }

    @Test
    void testSquareOfAPlaneIsTakenInsideItOnly() {
        final var camera = new Vector3f(10.f, 10.f, 10.f);
        final var size = size(camera);

        assertEquals(GizmoHandle.PlaneXZ, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 0.4f * size, 0.f, 0.4f * size), camera));
        assertNotEquals(GizmoHandle.PlaneXZ, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 0.9f * size, 0.f, 0.9f * size), camera));
    }

    @Test
    void testRingIsTakenOnItsLineOnly() {
        final var camera = new Vector3f(10.f, 10.f, 10.f);
        final var size = size(camera);
        final var half = (float) Math.sqrt(0.5) * size;

        assertEquals(GizmoHandle.AxisY, GizmoUtil.hit(GizmoMode.Rotate, ORIGIN, WORLD, toward(camera, half, 0.f, half), camera), "The ring round y");
        assertNull(GizmoUtil.hit(GizmoMode.Rotate, ORIGIN, WORLD, toward(camera, 0.3f * size, 0.f, 0.3f * size), camera), "Inside of the ring");
    }

    @Test
    void testScaleHasACubeAtTheEndOfEveryAxisAndOneInTheMiddle() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);

        assertEquals(GizmoHandle.AxisX, GizmoUtil.hit(GizmoMode.Scale, ORIGIN, WORLD, toward(camera, 1.4f, 0.f, 0.f), camera));
        assertEquals(GizmoHandle.Uniform, GizmoUtil.hit(GizmoMode.Scale, ORIGIN, WORLD, toward(camera, 0.05f, 0.f, 0.f), camera));
    }

    @Test
    void testAxesOfAThingThatIsTurnedAreItsOwn() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var turned = new Matrix3f().rotationZ((float) Math.toRadians(90.0));

        // The x axis of the thing points up the screen
        assertEquals(GizmoHandle.AxisX, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, turned, toward(camera, 0.f, 1.f, 0.f), camera));
    }

    @Test
    void testNearestHandleIsTakenWhenTheRayCrossesTwo() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);

        // The ray goes along z: the arrow of z is on it, and the arrows of x and y are not
        assertEquals(GizmoHandle.AxisZ, GizmoUtil.hit(GizmoMode.Translate, ORIGIN, WORLD, toward(camera, 0.f, 0.f, 0.f), camera));
    }

    @Test
    void testLinesDrawEveryHandleOfTheMode() {
        assertEquals(3 * (1 + 4 + 4), GizmoUtil.lines(GizmoMode.Translate, ORIGIN, WORLD, 1.f, null).size(), "An arrow and a square for every axis");
        assertEquals(3 * 48, GizmoUtil.lines(GizmoMode.Rotate, ORIGIN, WORLD, 1.f, null).size(), "A ring for every axis");
        assertEquals(3 * (1 + 12) + 12, GizmoUtil.lines(GizmoMode.Scale, ORIGIN, WORLD, 1.f, null).size(), "A line and a cube for every axis, and the cube in the middle");
    }

    @Test
    void testHighlightedHandleHasItsOwnColor() {
        final var lines = GizmoUtil.lines(GizmoMode.Rotate, ORIGIN, WORLD, 1.f, GizmoHandle.AxisY);

        assertTrue(lines.stream().anyMatch(line -> line.color() == GizmoUtil.HIGHLIGHT_COLOR));
        assertTrue(lines.stream().anyMatch(line -> line.color() == GizmoUtil.AXIS_COLORS[0]), "The others are as they were");
        assertEquals(48, lines.stream().filter(line -> line.color() == GizmoUtil.HIGHLIGHT_COLOR).count(), "Only the ring that is taken");
    }

    @Test
    void testDistanceAlongAnAxisIsWhereTheRayCrossesIt() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);

        assertEquals(2.f, GizmoUtil.alongAxis(toward(camera, 2.f, 0.f, 0.f), ORIGIN, new Vector3f(1.f, 0.f, 0.f)), EPSILON);
        assertEquals(-3.f, GizmoUtil.alongAxis(toward(camera, -3.f, 0.f, 0.f), ORIGIN, new Vector3f(1.f, 0.f, 0.f)), EPSILON);
        assertEquals(0.f, GizmoUtil.alongAxis(toward(camera, 0.f, 0.f, 0.f), ORIGIN, new Vector3f(0.f, 0.f, 1.f)), EPSILON, "A ray along the axis: no distance");
    }

    @Test
    void testDragAlongAnAxisMovesTheThingBySoMuchAsTheMouseMovedOnIt() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var drag = GizmoDrag.begin(GizmoMode.Translate, GizmoHandle.AxisX, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        final var result = drag.update(toward(camera, 2.5f, 0.f, 0.f), false);

        assertVector(new Vector3f(1.5f, 0.f, 0.f), result.translation());
        assertVector(new Vector3f(1.f), result.scale());
        assertEquals(new Matrix3f(), result.rotation());
    }

    @Test
    void testDragWithTheSnappingMovesByWholeSteps() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var drag = GizmoDrag.begin(GizmoMode.Translate, GizmoHandle.AxisX, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        assertEquals(0.f, drag.update(toward(camera, 1.1f, 0.f, 0.f), true).translation().x, EPSILON);
        assertEquals(0.25f, drag.update(toward(camera, 1.2f, 0.f, 0.f), true).translation().x, EPSILON);
        assertEquals(1.f, drag.update(toward(camera, 2.05f, 0.f, 0.f), true).translation().x, EPSILON);
    }

    @Test
    void testDragInAPlaneMovesAlongBothAxes() {
        final var camera = new Vector3f(0.f, 10.f, 0.f);
        final var drag = GizmoDrag.begin(GizmoMode.Translate, GizmoHandle.PlaneXZ, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 1.f), camera);

        final var result = drag.update(toward(camera, 2.f, 0.f, 3.f), false);

        assertVector(new Vector3f(1.f, 0.f, 2.f), result.translation());
    }

    @Test
    void testDragOfARingTurnsTheThingRoundTheAxisByTheAngleOfTheMouse() {
        final var camera = new Vector3f(0.f, 10.f, 0.f);
        final var drag = GizmoDrag.begin(GizmoMode.Rotate, GizmoHandle.AxisY, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        final var result = drag.update(toward(camera, 0.f, 0.f, 1.f), false);

        final var turned = result.rotation().transform(new Vector3f(1.f, 0.f, 0.f));
        assertEquals(0.f, turned.x, EPSILON);
        assertEquals(1.f, turned.z, EPSILON, "A quarter turn: x goes to z");
        assertTrue(result.translation().length() < EPSILON);
    }

    @Test
    void testTurnWithTheSnappingIsByWholeStepsOfFifteenDegrees() {
        final var camera = new Vector3f(0.f, 10.f, 0.f);
        final var drag = GizmoDrag.begin(GizmoMode.Rotate, GizmoHandle.AxisY, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        final var angle = (float) Math.toRadians(20.0);
        final var result = drag.update(toward(camera, (float) Math.cos(angle), 0.f, -(float) Math.sin(angle)), true);

        final var turned = result.rotation().transform(new Vector3f(1.f, 0.f, 0.f));
        assertEquals(Math.cos(Math.toRadians(15.0)), turned.x, EPSILON);
    }

    @Test
    void testTurnTakesTheShorterWayRoundTheSeam() {
        final var camera = new Vector3f(0.f, 10.f, 0.f);
        final var start = (float) Math.toRadians(170.0);
        final var end = (float) Math.toRadians(-170.0);
        final var drag = GizmoDrag.begin(GizmoMode.Rotate, GizmoHandle.AxisY, ORIGIN, WORLD,
                toward(camera, (float) Math.cos(start), 0.f, (float) Math.sin(start)), camera);

        final var result = drag.update(toward(camera, (float) Math.cos(end), 0.f, (float) Math.sin(end)), false);

        final var turned = result.rotation().transform(new Vector3f((float) Math.cos(start), 0.f, (float) Math.sin(start)));
        assertEquals(Math.cos(end), turned.x, EPSILON, "Twenty degrees, not three hundred and forty");
    }

    @Test
    void testDragOfACubeScalesAlongItsAxisByTheRatioOfTheDistances() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var drag = GizmoDrag.begin(GizmoMode.Scale, GizmoHandle.AxisX, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        assertVector(new Vector3f(2.f, 1.f, 1.f), drag.update(toward(camera, 2.f, 0.f, 0.f), false).scale());
        assertVector(new Vector3f(0.5f, 1.f, 1.f), drag.update(toward(camera, 0.5f, 0.f, 0.f), false).scale());
    }

    @Test
    void testScaleNeverGoesToZeroOrBelow() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var drag = GizmoDrag.begin(GizmoMode.Scale, GizmoHandle.AxisX, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        assertTrue(drag.update(toward(camera, -4.f, 0.f, 0.f), false).scale().x > 0.f);
        assertEquals(GizmoDrag.SCALE_STEP, drag.update(toward(camera, -4.f, 0.f, 0.f), true).scale().x, EPSILON);
    }

    @Test
    void testScaleWithTheSnappingIsByTenths() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var drag = GizmoDrag.begin(GizmoMode.Scale, GizmoHandle.AxisX, ORIGIN, WORLD, toward(camera, 1.f, 0.f, 0.f), camera);

        assertEquals(1.f, drag.update(toward(camera, 1.04f, 0.f, 0.f), true).scale().x, EPSILON);
        assertEquals(1.1f, drag.update(toward(camera, 1.06f, 0.f, 0.f), true).scale().x, EPSILON);
    }

    @Test
    void testMiddleCubeScalesAllTheAxes() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var drag = GizmoDrag.begin(GizmoMode.Scale, GizmoHandle.Uniform, ORIGIN, WORLD, toward(camera, 0.2f, 0.f, 0.f), camera);

        assertVector(new Vector3f(2.f), drag.update(toward(camera, 0.4f, 0.f, 0.f), false).scale());
    }

    @Test
    void testDragThatHasNotMovedDoesNothing() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var ray = toward(camera, 1.f, 0.f, 0.f);

        final var result = GizmoDrag.begin(GizmoMode.Translate, GizmoHandle.AxisX, ORIGIN, WORLD, ray, camera).update(ray, false);

        assertEquals(0.f, result.translation().length(), EPSILON);
        assertVector(new Vector3f(1.f), result.scale());
    }

    @Test
    void testDragAlongTheAxesOfAThingThatIsTurnedMovesInTheWorld() {
        final var camera = new Vector3f(0.f, 0.f, 10.f);
        final var turned = new Matrix3f().rotationZ((float) Math.toRadians(90.0));
        final var drag = GizmoDrag.begin(GizmoMode.Translate, GizmoHandle.AxisX, ORIGIN, turned, toward(camera, 0.f, 1.f, 0.f), camera);

        final var result = drag.update(toward(camera, 0.f, 3.f, 0.f), false);

        assertEquals(0.f, result.translation().x, EPSILON);
        assertEquals(2.f, result.translation().y, EPSILON, "The x axis of the thing is the y of the world");
    }
}
