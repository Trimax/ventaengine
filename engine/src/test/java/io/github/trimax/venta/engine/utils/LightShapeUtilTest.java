package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.gizmo.GizmoLine;
import io.github.trimax.venta.engine.model.common.shared.Cone;

final class LightShapeUtilTest {
    private static final float EPSILON = 1e-3f;
    private static final Vector3f ORIGIN = new Vector3f(1.f, 2.f, 3.f);
    private static final Vector3f DOWN = new Vector3f(0.f, -1.f, 0.f);

    @Test
    void testPointLightHasThreeCirclesOfItsRange() {
        final var lines = LightShapeUtil.lines(ORIGIN, 7.f, DOWN, null);

        assertEquals(3 * LightShapeUtil.SEGMENTS, lines.size());
        for (final var line : lines) {
            assertEquals(7.f, line.start().distance(ORIGIN), EPSILON, "Every point is on the sphere of the range");
            assertEquals(7.f, line.end().distance(ORIGIN), EPSILON);
        }
    }

    @Test
    void testPointLightWithoutARangeHasNoShape() {
        assertTrue(LightShapeUtil.lines(ORIGIN, 0.f, DOWN, null).isEmpty());
    }

    @Test
    void testCirclesAreClosed() {
        final var lines = LightShapeUtil.lines(ORIGIN, 4.f, DOWN, null).subList(0, LightShapeUtil.SEGMENTS);

        assertEquals(0.f, lines.getFirst().start().distance(lines.getLast().end()), EPSILON);
    }

    @Test
    void testEverySegmentStartsWhereThePreviousEnded() {
        final var lines = LightShapeUtil.lines(ORIGIN, 4.f, DOWN, null);

        for (int index = 1; index < LightShapeUtil.SEGMENTS; index++)
            assertEquals(0.f, lines.get(index - 1).end().distance(lines.get(index).start()), EPSILON);
    }

    @Test
    void testSpotLightHasAConeAlongItsDirection() {
        final var lines = LightShapeUtil.lines(ORIGIN, 10.f, DOWN, new Cone(20.f, 30.f));

        // The outer circle, its axis and its edges, the inner circle
        assertEquals(LightShapeUtil.SEGMENTS + 1 + LightShapeUtil.EDGES + LightShapeUtil.SEGMENTS, lines.size());

        final var axis = lines.stream().filter(line -> line.start().equals(ORIGIN) && Math.abs(line.end().x - ORIGIN.x) < EPSILON && Math.abs(line.end().z - ORIGIN.z) < EPSILON).findFirst().orElseThrow();
        assertEquals(ORIGIN.y - 10.f, axis.end().y, EPSILON, "The axis goes down, as far as the range");
    }

    @Test
    void testEdgesOfTheConeAreAsLongAsTheRangeAndLieOnTheOuterAngle() {
        final var lines = LightShapeUtil.lines(ORIGIN, 10.f, DOWN, new Cone(20.f, 30.f));

        final var edges = lines.stream().filter(line -> line.start().equals(ORIGIN) && line.end().distance(ORIGIN) > 0.f).toList();
        assertEquals(1 + LightShapeUtil.EDGES, edges.size());

        for (final var edge : edges.subList(1, edges.size())) {
            final var along = new Vector3f(edge.end()).sub(ORIGIN);
            assertEquals(10.f, along.length(), EPSILON);
            assertEquals(30.0, Math.toDegrees(Math.acos(along.normalize().dot(DOWN))), 0.05, "The angle to the direction is the outer angle");
        }
    }

    @Test
    void testInnerCircleIsInTheDimmerColor() {
        final var lines = LightShapeUtil.lines(ORIGIN, 10.f, DOWN, new Cone(20.f, 30.f));

        assertEquals(LightShapeUtil.SEGMENTS, lines.stream().filter((GizmoLine line) -> line.color() == LightShapeUtil.INNER_COLOR).count());
    }

    @Test
    void testSpotLightWithoutARangeIsDrawnWithADefaultLength() {
        final var lines = LightShapeUtil.lines(ORIGIN, 0.f, DOWN, new Cone(20.f, 30.f));

        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().anyMatch(line -> Math.abs(line.end().distance(ORIGIN) - LightShapeUtil.DEFAULT_LENGTH) < EPSILON));
    }

    @Test
    void testConeWorksForADirectionAlongTheVerticalAxisAndAnyOther() {
        for (final var direction : new Vector3f[]{new Vector3f(0.f, 1.f, 0.f), new Vector3f(1.f, 0.f, 0.f), new Vector3f(1.f, 1.f, 1.f)})
            for (final var line : LightShapeUtil.lines(ORIGIN, 5.f, direction, new Cone(10.f, 40.f))) {
                assertTrue(Float.isFinite(line.start().length()) && Float.isFinite(line.end().length()), "No NaN for " + direction);
                assertTrue(line.start().distance(ORIGIN) <= 5.f + EPSILON && line.end().distance(ORIGIN) <= 5.f + EPSILON, "Inside the sphere of the range");
            }
    }

    private static io.github.trimax.venta.engine.model.common.geo.Ray toward(final Vector3f camera, final Vector3f target) {
        return new io.github.trimax.venta.engine.model.common.geo.Ray(new Vector3f(camera), new Vector3f(target).sub(camera).normalize());
    }

    @Test
    void testPointLightHasAHandleOfTheRangeOnEveryAxis() {
        final var handles = LightShapeUtil.handles(ORIGIN, 6.f, DOWN, null);

        assertEquals(3, handles.size());
        assertEquals(new Vector3f(7.f, 2.f, 3.f), handles.get(0).point());
        assertEquals(new Vector3f(1.f, 8.f, 3.f), handles.get(1).point());
        assertEquals(new Vector3f(1.f, 2.f, 9.f), handles.get(2).point());
        assertTrue(handles.stream().allMatch(handle -> handle.kind() == io.github.trimax.venta.engine.model.common.gizmo.LightHandle.Range));
    }

    @Test
    void testPointLightWithoutARangeHasNoHandles() {
        assertTrue(LightShapeUtil.handles(ORIGIN, 0.f, DOWN, null).isEmpty());
    }

    @Test
    void testSpotLightHasAHandleOfTheRangeAndOneOnEveryEdgeOfTheCone() {
        final var handles = LightShapeUtil.handles(ORIGIN, 10.f, DOWN, new Cone(20.f, 30.f));
        final var kinds = io.github.trimax.venta.engine.model.common.gizmo.LightHandle.values();

        assertEquals(3, handles.size());
        assertEquals(kinds[0], handles.get(0).kind());
        assertEquals(kinds[1], handles.get(1).kind());
        assertEquals(kinds[2], handles.get(2).kind());
        assertEquals(ORIGIN.y - 10.f, handles.get(0).point().y, EPSILON, "The range is at the end of the axis");

        for (int index = 1; index < 3; index++) {
            final var along = new Vector3f(handles.get(index).point()).sub(ORIGIN);
            assertEquals(10.f, along.length(), EPSILON, "On the sphere of the range");
            assertEquals(index == 1 ? 30.0 : 20.0, Math.toDegrees(Math.acos(along.normalize().dot(DOWN))), 0.05);
        }
    }

    @Test
    void testHandleIsTakenWhereTheRayGoesNearIt() {
        final var camera = new Vector3f(1.f, 2.f, 30.f);
        final var handles = LightShapeUtil.handles(ORIGIN, 6.f, DOWN, null);

        assertEquals(0, LightShapeUtil.hit(handles, toward(camera, handles.get(0).point()), camera).index());
        assertEquals(1, LightShapeUtil.hit(handles, toward(camera, handles.get(1).point()), camera).index());
        assertNull(LightShapeUtil.hit(handles, toward(camera, new Vector3f(1.f, 2.f, 3.f).add(-3.f, -3.f, 0.f)), camera));
    }

    @Test
    void testHandlesAreCubesAndTheLitOneHasItsColor() {
        final var camera = new Vector3f(1.f, 2.f, 30.f);
        final var handles = LightShapeUtil.handles(ORIGIN, 6.f, DOWN, null);

        final var lines = LightShapeUtil.handleLines(handles, camera, 1);

        assertEquals(3 * 12, lines.size());
        assertEquals(12, lines.stream().filter(line -> line.color() == LightShapeUtil.HIGHLIGHT_COLOR).count());
        assertEquals(24, lines.stream().filter(line -> line.color() == LightShapeUtil.COLOR).count());
    }
}
