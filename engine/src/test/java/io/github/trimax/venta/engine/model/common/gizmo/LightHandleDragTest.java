package io.github.trimax.venta.engine.model.common.gizmo;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.model.common.shared.Cone;
import io.github.trimax.venta.engine.utils.LightShapeUtil;

final class LightHandleDragTest {
    private static final float EPSILON = 1e-2f;
    private static final Vector3f ORIGIN = new Vector3f();
    private static final Vector3f DOWN = new Vector3f(0.f, -1.f, 0.f);

    private static Ray toward(final Vector3f camera, final Vector3f target) {
        return new Ray(new Vector3f(camera), new Vector3f(target).sub(camera).normalize());
    }

    /** A point of the plane x = 0 at an angle from the axis that points down (the plane of the handles of the angles of a light that shines down) */
    private static Vector3f at(final float degrees, final float distance) {
        final var angle = (float) Math.toRadians(degrees);
        return new Vector3f(0.f, -(float) Math.cos(angle) * distance, (float) Math.sin(angle) * distance);
    }

    private static LightShapeUtil.Handle handleOf(final LightHandle kind) {
        return LightShapeUtil.handles(ORIGIN, 10.f, DOWN, new Cone(20.f, 30.f)).stream().filter(handle -> handle.kind() == kind).findFirst().orElseThrow();
    }

    @Test
    void testRangeFollowsTheMouseAlongTheAxis() {
        final var camera = new Vector3f(0.f, 0.f, 30.f);
        final var handle = LightShapeUtil.handles(ORIGIN, 5.f, DOWN, null).getFirst();
        final var drag = LightHandleDrag.begin(handle, ORIGIN, 5.f, toward(camera, new Vector3f(5.f, 0.f, 0.f)));

        assertEquals(8.f, drag.update(toward(camera, new Vector3f(8.f, 0.f, 0.f)), false), EPSILON);
        assertEquals(2.f, drag.update(toward(camera, new Vector3f(2.f, 0.f, 0.f)), false), EPSILON);
    }

    @Test
    void testRangeKeepsTheOffsetOfTheMouseFromTheHandle() {
        final var camera = new Vector3f(0.f, 0.f, 30.f);
        final var handle = LightShapeUtil.handles(ORIGIN, 5.f, DOWN, null).getFirst();

        // The mouse took the handle a meter short of its center: it doesn't jump when it moves
        final var drag = LightHandleDrag.begin(handle, ORIGIN, 5.f, toward(camera, new Vector3f(4.f, 0.f, 0.f)));

        assertEquals(5.f, drag.update(toward(camera, new Vector3f(4.f, 0.f, 0.f)), false), EPSILON);
        assertEquals(6.f, drag.update(toward(camera, new Vector3f(5.f, 0.f, 0.f)), false), EPSILON);
    }

    @Test
    void testRangeWithTheSnappingIsInWholeStepsAndNeverZero() {
        final var camera = new Vector3f(0.f, 0.f, 30.f);
        final var handle = LightShapeUtil.handles(ORIGIN, 5.f, DOWN, null).getFirst();
        final var drag = LightHandleDrag.begin(handle, ORIGIN, 5.f, toward(camera, new Vector3f(5.f, 0.f, 0.f)));

        assertEquals(5.f, drag.update(toward(camera, new Vector3f(5.2f, 0.f, 0.f)), true), EPSILON);
        assertEquals(5.5f, drag.update(toward(camera, new Vector3f(5.3f, 0.f, 0.f)), true), EPSILON);
        assertTrue(drag.update(toward(camera, new Vector3f(-9.f, 0.f, 0.f)), false) >= LightHandleDrag.MINIMAL_RANGE);
        assertTrue(drag.update(toward(camera, new Vector3f(-9.f, 0.f, 0.f)), true) >= LightHandleDrag.RANGE_STEP);
    }

    @Test
    void testAngleFollowsTheMouseAroundTheApex() {
        final var camera = new Vector3f(30.f, 0.f, 0.f);
        final var handle = handleOf(LightHandle.Outer);
        final var drag = LightHandleDrag.begin(handle, ORIGIN, 30.f, toward(camera, at(30.f, 10.f)));

        assertEquals(30.f, drag.update(toward(camera, at(30.f, 10.f)), false), EPSILON);
        assertEquals(45.f, drag.update(toward(camera, at(45.f, 4.f)), false), EPSILON, "The angle, not the distance, counts");
        assertEquals(12.f, drag.update(toward(camera, at(12.f, 20.f)), false), EPSILON);
    }

    @Test
    void testAngleWithTheSnappingIsInWholeStepsAndStaysInTheRange() {
        final var camera = new Vector3f(30.f, 0.f, 0.f);
        final var drag = LightHandleDrag.begin(handleOf(LightHandle.Inner), ORIGIN, 20.f, toward(camera, at(20.f, 10.f)));

        assertEquals(45.f, drag.update(toward(camera, at(47.f, 10.f)), true), EPSILON);
        assertEquals(LightHandleDrag.MAXIMAL_ANGLE, drag.update(toward(camera, at(120.f, 10.f)), false), EPSILON);
        assertEquals(LightHandleDrag.MINIMAL_ANGLE, drag.update(toward(camera, at(-30.f, 10.f)), false), EPSILON);
    }

    @Test
    void testAngleDoesNotChangeWhenTheRayIsAlongThePlane() {
        // The plane of the handles faces x: a ray along it is parallel to it
        final var camera = new Vector3f(0.f, 0.f, 30.f);
        final var drag = LightHandleDrag.begin(handleOf(LightHandle.Outer), ORIGIN, 30.f, toward(camera, at(30.f, 10.f)));

        assertEquals(30.f, drag.update(new Ray(new Vector3f(0.f, 5.f, 30.f), new Vector3f(0.f, 0.f, -1.f)), false), EPSILON);
    }
}
