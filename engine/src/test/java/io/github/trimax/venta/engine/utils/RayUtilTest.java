package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Ray;

final class RayUtilTest {
    private static final float EPSILON = 1e-4f;
    private static final BoundingBox CUBE = new BoundingBox(new Vector3f(-1.f), new Vector3f(1.f));

    private static Ray ray(final float x, final float y, final float z, final float dx, final float dy, final float dz) {
        return new Ray(new Vector3f(x, y, z), new Vector3f(dx, dy, dz).normalize());
    }

    @Test
    void testRayThroughTheMiddleOfTheScreenGoesAlongTheViewAxis() {
        final var view = new Matrix4f().lookAt(0.f, 0.f, 10.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
        final var projection = new Matrix4f().perspective((float) Math.toRadians(60.0), 1.f, 0.1f, 100.f);
        final var ray = RayUtil.through(0.f, 0.f, new Matrix4f(projection).mul(view).invert());

        assertEquals(0.f, ray.direction().x, EPSILON);
        assertEquals(0.f, ray.direction().y, EPSILON);
        assertEquals(-1.f, ray.direction().z, EPSILON);
        assertEquals(9.9f, ray.origin().z, EPSILON, "It starts at the near plane");
    }

    @Test
    void testRayThroughTheRightEdgeGoesToTheRight() {
        final var view = new Matrix4f().lookAt(0.f, 0.f, 10.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
        final var projection = new Matrix4f().perspective((float) Math.toRadians(90.0), 1.f, 0.1f, 100.f);
        final var ray = RayUtil.through(1.f, 0.f, new Matrix4f(projection).mul(view).invert());

        assertEquals(Math.sqrt(0.5), ray.direction().x, EPSILON, "The edge of a 90 degree field is at 45 degrees");
        assertEquals(-Math.sqrt(0.5), ray.direction().z, EPSILON);
        assertEquals(1.f, ray.direction().length(), EPSILON);
    }

    @Test
    void testRayMeetsTheNearFaceOfABox() {
        assertEquals(4.f, RayUtil.intersectBox(ray(0.f, 0.f, 5.f, 0.f, 0.f, -1.f), CUBE, new Matrix4f()), EPSILON);
    }

    @Test
    void testRayThatPassesByABoxMissesIt() {
        assertEquals(RayUtil.MISS, RayUtil.intersectBox(ray(2.f, 0.f, 5.f, 0.f, 0.f, -1.f), CUBE, new Matrix4f()));
        assertEquals(RayUtil.MISS, RayUtil.intersectBox(ray(0.f, 0.f, 5.f, 1.f, 0.f, 0.f), CUBE, new Matrix4f()), "Parallel to the faces, outside");
    }

    @Test
    void testRayThatStartsInsideMeetsTheBoxAtOnce() {
        assertEquals(0.f, RayUtil.intersectBox(ray(0.f, 0.f, 0.f, 0.f, 0.f, -1.f), CUBE, new Matrix4f()));
    }

    @Test
    void testBoxBehindTheStartIsNotMet() {
        assertEquals(RayUtil.MISS, RayUtil.intersectBox(ray(0.f, 0.f, 5.f, 0.f, 0.f, 1.f), CUBE, new Matrix4f()));
    }

    @Test
    void testBoxIsMovedAndScaledByItsMatrix() {
        final var model = new Matrix4f().translate(10.f, 0.f, 0.f).scale(2.f);

        assertEquals(8.f, RayUtil.intersectBox(ray(10.f, 0.f, 10.f, 0.f, 0.f, -1.f), CUBE, model), EPSILON, "The face is at z = 2");
        assertEquals(RayUtil.MISS, RayUtil.intersectBox(ray(0.f, 0.f, 10.f, 0.f, 0.f, -1.f), CUBE, model), "The box is not where it was");
    }

    @Test
    void testBoxIsTurnedByItsMatrix() {
        final var model = new Matrix4f().rotateY((float) Math.toRadians(45.0));

        // A square of half-size 1 turned by 45 degrees reaches sqrt(2) along the axes
        assertEquals(5.f - (float) Math.sqrt(2.0), RayUtil.intersectBox(ray(-5.f, 0.f, 0.f, 1.f, 0.f, 0.f), CUBE, model), EPSILON);
    }

    @Test
    void testBoxThatIsNotStraightIsMetAlongItsOwnAxes() {
        final var thin = new BoundingBox(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f));
        final var model = new Matrix4f().scale(4.f, 1.f, 1.f);

        assertEquals(3.f, RayUtil.intersectBox(ray(-5.f, 0.f, 0.f, 1.f, 0.f, 0.f), thin, model), EPSILON, "The box reaches 2 meters to each side");
    }

    @Test
    void testRayMeetsASphere() {
        assertEquals(4.f, RayUtil.intersectSphere(ray(0.f, 0.f, 5.f, 0.f, 0.f, -1.f), new Vector3f(), 1.f), EPSILON);
        assertEquals(RayUtil.MISS, RayUtil.intersectSphere(ray(0.f, 2.f, 5.f, 0.f, 0.f, -1.f), new Vector3f(), 1.f));
        assertEquals(0.f, RayUtil.intersectSphere(ray(0.f, 0.f, 0.f, 0.f, 0.f, -1.f), new Vector3f(), 1.f), "A start inside");
        assertEquals(RayUtil.MISS, RayUtil.intersectSphere(ray(0.f, 0.f, 5.f, 0.f, 0.f, 1.f), new Vector3f(), 1.f), "Behind");
    }

    @Test
    void testRayMeetsAPlane() {
        assertEquals(5.f, RayUtil.intersectPlane(ray(0.f, 5.f, 0.f, 0.f, -1.f, 0.f), new Vector3f(), new Vector3f(0.f, 1.f, 0.f)), EPSILON);
        assertEquals(5.f * (float) Math.sqrt(2.0), RayUtil.intersectPlane(ray(0.f, 5.f, 0.f, 1.f, -1.f, 0.f), new Vector3f(), new Vector3f(0.f, 1.f, 0.f)), EPSILON, "A slanted ray");
    }

    @Test
    void testRayThatDoesNotCrossAPlaneMissesIt() {
        assertEquals(RayUtil.MISS, RayUtil.intersectPlane(ray(0.f, 5.f, 0.f, 1.f, 0.f, 0.f), new Vector3f(), new Vector3f(0.f, 1.f, 0.f)), "Parallel");
        assertEquals(RayUtil.MISS, RayUtil.intersectPlane(ray(0.f, 5.f, 0.f, 0.f, 1.f, 0.f), new Vector3f(), new Vector3f(0.f, 1.f, 0.f)), "Away from it");
        assertEquals(0.f, RayUtil.intersectPlane(ray(3.f, 0.f, 0.f, 0.f, -1.f, 0.f), new Vector3f(), new Vector3f(0.f, 1.f, 0.f)), EPSILON, "A start on the plane");
    }
}
