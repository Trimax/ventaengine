package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Ray;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Rays for choosing what is under a point of the screen: the ray of a pixel, and where it meets a box or a sphere.
 */
@UtilityClass
public final class RayUtil {
    /** What a ray that meets nothing is told with */
    public static final float MISS = -1.f;

    /**
     * The ray that goes through a point of the screen.
     *
     * @param ndcX                  the point from -1 (left) to 1 (right)
     * @param ndcY                  the point from -1 (bottom) to 1 (top)
     * @param inverseViewProjection the inverse of the projection by the view of the camera
     */
    public Ray through(final float ndcX, final float ndcY, @NonNull final Matrix4fc inverseViewProjection) {
        final var near = unproject(ndcX, ndcY, -1.f, inverseViewProjection);
        final var far = unproject(ndcX, ndcY, 1.f, inverseViewProjection);

        return new Ray(near, far.sub(near).normalize());
    }

    /**
     * The distance from the start of the ray to a box that is moved, turned and scaled by a matrix.
     *
     * @param box   the box before the matrix is applied
     * @param model the matrix of the thing the box belongs to
     * @return the distance along the ray, {@link #MISS} when the ray doesn't meet the box (a box behind the start is not met)
     */
    public float intersectBox(@NonNull final Ray ray, @NonNull final BoundingBox box, @NonNull final Matrix4fc model) {
        // The ray goes into the space of the box, where the box is straight
        final var inverse = new Matrix4f(model).invert();
        final var origin = inverse.transformPosition(new Vector3f(ray.origin()));
        final var direction = inverse.transformDirection(new Vector3f(ray.direction()));

        var near = Float.NEGATIVE_INFINITY;
        var far = Float.POSITIVE_INFINITY;
        for (int axis = 0; axis < 3; axis++) {
            final var start = origin.get(axis);
            final var step = direction.get(axis);
            final var minimum = box.min().get(axis);
            final var maximum = box.max().get(axis);

            if (Math.abs(step) < 1e-9f) {
                // Parallel to the faces of this axis: it meets the box only between them
                if (start < minimum || start > maximum)
                    return MISS;

                continue;
            }

            var first = (minimum - start) / step;
            var second = (maximum - start) / step;
            if (first > second) {
                final var swap = first;
                first = second;
                second = swap;
            }

            near = Math.max(near, first);
            far = Math.min(far, second);
            if (near > far)
                return MISS;
        }

        if (far < 0.f)
            return MISS;

        // A start inside the box meets it at once. The point of the ray at a parameter is the same in both spaces (the matrix is linear
        // and the direction is a unit vector), so the parameter is the distance
        return Math.max(near, 0.f);
    }

    /**
     * The distance from the start of the ray to a plane.
     *
     * @param point  a point of the plane
     * @param normal the normal of the plane
     * @return the distance along the ray, {@link #MISS} when the ray is parallel to the plane or goes away from it
     */
    public float intersectPlane(@NonNull final Ray ray, @NonNull final Vector3fc point, @NonNull final Vector3fc normal) {
        final var slope = normal.dot(ray.direction());
        if (Math.abs(slope) < 1e-6f)
            return MISS;

        final var distance = new Vector3f(point).sub(ray.origin()).dot(normal) / slope;
        return distance < 0.f ? MISS : distance;
    }

    /**
     * The distance from the start of the ray to a sphere, {@link #MISS} when the ray doesn't meet it.
     */
    public float intersectSphere(@NonNull final Ray ray, @NonNull final Vector3fc center, final float radius) {
        final var offset = new Vector3f(center).sub(ray.origin());
        final var along = offset.dot(ray.direction());
        final var closest = offset.lengthSquared() - along * along;
        if (closest > radius * radius)
            return MISS;

        final var half = (float) Math.sqrt(radius * radius - closest);
        final var near = along - half;
        final var far = along + half;
        if (far < 0.f)
            return MISS;

        return Math.max(near, 0.f);
    }

    private Vector3f unproject(final float x, final float y, final float z, final Matrix4fc inverse) {
        final var point = inverse.transform(new Vector4f(x, y, z, 1.f));
        return new Vector3f(point.x / point.w, point.y / point.w, point.z / point.w);
    }
}
