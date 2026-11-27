package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.Ray;

final class GroundUtilTest {
    private static final float EPSILON = 1e-2f;
    private static final float FAR = GroundUtil.MAXIMAL_DISTANCE;

    private static Ray ray(final float x, final float y, final float z, final float dx, final float dy, final float dz) {
        return new Ray(new Vector3f(x, y, z), new Vector3f(dx, dy, dz).normalize());
    }

    @Test
    void testRayThatGoesDownMeetsFlatGroundAtItsHeight() {
        final var distance = GroundUtil.intersect(ray(0.f, 10.f, 0.f, 0.f, -1.f, 0.f), (x, z) -> 2.f, point -> true, FAR);

        assertEquals(8.f, distance, EPSILON);
    }

    @Test
    void testSlantedRayMeetsGroundAtThePointOnTheRay() {
        final var ray = ray(0.f, 10.f, 0.f, 1.f, -1.f, 0.f);

        final var distance = GroundUtil.intersect(ray, (x, z) -> 0.f, point -> true, FAR);

        assertEquals(10.f * (float) Math.sqrt(2.0), distance, EPSILON);
    }

    @Test
    void testGroundThatRisesIsMetWhereTheRayGoesUnderIt() {
        // The ground is a ramp: y = x / 2, the ray is level at y = 5, so it meets the ramp at x = 10
        final var distance = GroundUtil.intersect(ray(0.f, 5.f, 0.f, 1.f, 0.f, 0.f), (x, z) -> x * 0.5f, point -> true, FAR);

        assertEquals(10.f, distance, EPSILON);
    }

    @Test
    void testRayThatNeverGoesUnderTheGroundMissesIt() {
        assertEquals(RayUtil.MISS, GroundUtil.intersect(ray(0.f, 10.f, 0.f, 1.f, 0.f, 0.f), (x, z) -> 0.f, point -> true, FAR));
        assertEquals(RayUtil.MISS, GroundUtil.intersect(ray(0.f, 10.f, 0.f, 0.f, 1.f, 0.f), (x, z) -> 0.f, point -> true, FAR), "Up from the ground");
    }

    @Test
    void testRayThatIsOnlyOverTheGroundBesideTheAreaMissesIt() {
        // The area is x from -5 to 5, the ray goes down at x = 20
        final var distance = GroundUtil.intersect(ray(20.f, 10.f, 0.f, 0.f, -1.f, 0.f), (x, z) -> 0.f, point -> Math.abs(point.x()) <= 5.f, FAR);

        assertEquals(RayUtil.MISS, distance);
    }

    @Test
    void testRayThatComesIntoTheAreaUnderTheGroundIsMetAtTheEdge() {
        // The ground is a block of height 3 in x from 0 to 10: a ray that is level at y = 1 meets its side at x = 0
        final var distance = GroundUtil.intersect(ray(-10.f, 1.f, 0.f, 1.f, 0.f, 0.f), (x, z) -> 3.f, point -> point.x() >= 0.f && point.x() <= 10.f, FAR);

        assertEquals(10.f, distance, 0.3f);
    }

    @Test
    void testFarGroundIsFoundWithLongerSteps() {
        final var distance = GroundUtil.intersect(ray(0.f, 1000.f, 0.f, 1.f, -0.5f, 0.f), (x, z) -> 0.f, point -> true, FAR);

        assertEquals(2000.f * (float) Math.sqrt(1.25), distance, 1.f);
    }

    @Test
    void testGroundBeyondTheDistanceIsNotFound() {
        assertEquals(RayUtil.MISS, GroundUtil.intersect(ray(0.f, 100.f, 0.f, 0.f, -1.f, 0.f), (x, z) -> 0.f, point -> true, 50.f));
    }

    @Test
    void testNarrowingGivesTheSurfaceNotTheStep() {
        // A ray with long steps (far from the start) still lands on the surface, not a step beyond it
        final var ray = ray(0.f, 500.f, 0.f, 0.f, -1.f, 0.f);

        assertEquals(499.f, GroundUtil.intersect(ray, (x, z) -> 1.f, point -> true, FAR), 0.01f);
    }
}
