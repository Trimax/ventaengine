package io.github.trimax.venta.engine.utils;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoHandle;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoLine;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The handles over a chosen thing of a scene: where they are, which of them is under a ray, and the lines that draw them. The handles
 * are as big on the screen whatever the distance to them is: their size is a part of it. The axes of the handles are the columns of a matrix:
 * the axes of the world, or the axes of the thing.
 */
@UtilityClass
public final class GizmoUtil {
    /** The length of an axis handle as a part of the distance to the thing, and the least length (meters) */
    public static final float SCREEN_FRACTION = 0.14f;
    public static final float MINIMAL_SIZE = 0.05f;

    /** The colors of the axes x, y and z, and of a handle that is under the mouse or is taken */
    public static final float[][] AXIS_COLORS = {{0.92f, 0.30f, 0.30f}, {0.42f, 0.80f, 0.40f}, {0.36f, 0.56f, 0.96f}};
    public static final float[] HIGHLIGHT_COLOR = {1.f, 0.85f, 0.20f};
    public static final float[] UNIFORM_COLOR = {0.88f, 0.88f, 0.90f};

    /** How near to a line the ray goes to take it, as a part of the length of the axis */
    private static final float LINE_TOLERANCE = 0.08f;

    private static final float PLANE_START = 0.28f;
    private static final float PLANE_END = 0.52f;
    private static final float ARROW_START = 0.2f;
    private static final float RING_TOLERANCE = 0.09f;
    private static final float CENTER_RADIUS = 0.13f;
    private static final float CUBE_HALF = 0.07f;
    private static final int RING_SEGMENTS = 48;

    /** The length of the axis handles of a thing that is seen from the place */
    public float size(@NonNull final Vector3fc origin, @NonNull final Vector3fc camera) {
        return Math.max(new Vector3f(origin).distance(camera) * SCREEN_FRACTION, MINIMAL_SIZE);
    }

    /** The axis of the matrix: its column */
    public Vector3f axis(@NonNull final Matrix3fc axes, final int index) {
        return axes.getColumn(index, new Vector3f()).normalize();
    }

    /**
     * The handle under the ray.
     *
     * @return the nearest handle that the ray takes, null when it takes none
     */
    public GizmoHandle hit(@NonNull final GizmoMode mode, @NonNull final Vector3fc origin, @NonNull final Matrix3fc axes, @NonNull final Ray ray, @NonNull final Vector3fc camera) {
        final var size = size(origin, camera);

        GizmoHandle best = null;
        var nearest = Float.MAX_VALUE;

        if (mode == GizmoMode.Scale) {
            final var distance = RayUtil.intersectSphere(ray, origin, size * CENTER_RADIUS);
            // The cube in the middle is taken before the lines that start in it
            if (distance != RayUtil.MISS)
                return GizmoHandle.Uniform;
        }

        for (int index = 0; index < 3; index++) {
            final var axis = axis(axes, index);
            final var distance = switch (mode) {
                case Translate -> segment(ray, origin, axis, size * ARROW_START, size, size * LINE_TOLERANCE);
                case Scale -> segment(ray, origin, axis, size * CENTER_RADIUS, size, size * LINE_TOLERANCE);
                case Rotate -> ring(ray, origin, axis, size);
            };

            if (distance != RayUtil.MISS && distance < nearest) {
                best = GizmoHandle.ofAxis(index);
                nearest = distance;
            }

            if (mode == GizmoMode.Translate) {
                final var plane = plane(ray, origin, axes, index, size);
                if (plane != RayUtil.MISS && plane < nearest) {
                    best = GizmoHandle.ofPlane(index);
                    nearest = plane;
                }
            }
        }

        return best;
    }

    /**
     * The lines that draw the handles of the mode.
     *
     * @param highlighted the handle that is under the mouse or taken, it is drawn in another color; null for none
     */
    public List<GizmoLine> lines(@NonNull final GizmoMode mode, @NonNull final Vector3fc origin, @NonNull final Matrix3fc axes, final float size, final GizmoHandle highlighted) {
        final var lines = new ArrayList<GizmoLine>();

        for (int index = 0; index < 3; index++) {
            final var axis = axis(axes, index);
            final var color = color(GizmoHandle.ofAxis(index), highlighted, AXIS_COLORS[index]);

            switch (mode) {
                case Translate -> {
                    arrow(lines, origin, axis, axis(axes, (index + 1) % 3), axis(axes, (index + 2) % 3), size, color);
                    square(lines, origin, axis(axes, (index + 1) % 3), axis(axes, (index + 2) % 3), size, color(GizmoHandle.ofPlane(index), highlighted, AXIS_COLORS[index]));
                }
                case Rotate -> circle(lines, origin, axis, axis(axes, (index + 1) % 3), axis(axes, (index + 2) % 3), size, color);
                case Scale -> {
                    line(lines, new Vector3f(origin).fma(size * CENTER_RADIUS, axis), new Vector3f(origin).fma(size, axis), color);
                    cube(lines, new Vector3f(origin).fma(size, axis), size * CUBE_HALF, axes, color);
                }
                default -> {
                }
            }
        }

        if (mode == GizmoMode.Scale)
            cube(lines, origin, size * CENTER_RADIUS, axes, color(GizmoHandle.Uniform, highlighted, UNIFORM_COLOR));

        return lines;
    }

    private float[] color(final GizmoHandle handle, final GizmoHandle highlighted, final float[] normal) {
        return handle == highlighted ? HIGHLIGHT_COLOR : normal;
    }

    private void line(final List<GizmoLine> lines, final Vector3f start, final Vector3f end, final float[] color) {
        lines.add(new GizmoLine(start, end, color));
    }

    /** A line with a head: the head is four lines from the tip back to a square round the line */
    private void arrow(final List<GizmoLine> lines, final Vector3fc origin, final Vector3f axis, final Vector3f u, final Vector3f v, final float size, final float[] color) {
        final var tip = new Vector3f(origin).fma(size, axis);
        final var base = new Vector3f(origin).fma(size * 0.8f, axis);
        final var radius = size * 0.06f;

        line(lines, new Vector3f(origin).fma(size * ARROW_START, axis), tip, color);
        for (final var side : new Vector3f[]{u, v})
            for (final var sign : new float[]{-1.f, 1.f})
                line(lines, new Vector3f(tip), new Vector3f(base).fma(sign * radius, side), color);
    }

    /**
     * The outline of the square of a plane handle: the plane of the two axes, between {@link #PLANE_START} and {@link #PLANE_END} of the size.
     */
    private void square(final List<GizmoLine> lines, final Vector3fc origin, final Vector3f u, final Vector3f v, final float size, final float[] color) {
        final var a = new Vector3f(origin).fma(size * PLANE_START, u).fma(size * PLANE_START, v);
        final var b = new Vector3f(origin).fma(size * PLANE_END, u).fma(size * PLANE_START, v);
        final var c = new Vector3f(origin).fma(size * PLANE_END, u).fma(size * PLANE_END, v);
        final var d = new Vector3f(origin).fma(size * PLANE_START, u).fma(size * PLANE_END, v);

        line(lines, a, b, color);
        line(lines, b, c, color);
        line(lines, c, d, color);
        line(lines, d, a, color);
    }

    private void circle(final List<GizmoLine> lines, final Vector3fc origin, final Vector3f axis, final Vector3f u, final Vector3f v, final float size, final float[] color) {
        Vector3f previous = null;
        for (int step = 0; step <= RING_SEGMENTS; step++) {
            final var angle = (float) (2.0 * Math.PI * step / RING_SEGMENTS);
            final var point = new Vector3f(origin).fma(size * (float) Math.cos(angle), u).fma(size * (float) Math.sin(angle), v);

            if (previous != null)
                line(lines, previous, point, color);

            previous = point;
        }
    }

    private void cube(final List<GizmoLine> lines, final Vector3fc center, final float half, final Matrix3fc axes, final float[] color) {
        final var corners = new Vector3f[8];
        for (int index = 0; index < 8; index++)
            corners[index] = new Vector3f(center)
                    .fma((index & 1) == 0 ? -half : half, axis(axes, 0))
                    .fma((index & 2) == 0 ? -half : half, axis(axes, 1))
                    .fma((index & 4) == 0 ? -half : half, axis(axes, 2));

        for (int index = 0; index < 8; index++)
            for (int bit = 1; bit <= 4; bit <<= 1)
                if ((index & bit) == 0)
                    line(lines, new Vector3f(corners[index]), new Vector3f(corners[index | bit]), color);
    }

    /** The distance along the ray to the segment of an axis, when the ray goes near it */
    private float segment(final Ray ray, final Vector3fc origin, final Vector3f axis, final float from, final float to, final float tolerance) {
        final var start = new Vector3f(origin).fma(from, axis);
        final var end = new Vector3f(origin).fma(to, axis);
        final var closest = closest(ray, start, end);

        return closest[0] <= tolerance ? closest[1] : RayUtil.MISS;
    }

    /** The distance along the ray to a ring round an axis, when the ray crosses the plane of it near the ring */
    private float ring(final Ray ray, final Vector3fc origin, final Vector3f axis, final float size) {
        final var distance = RayUtil.intersectPlane(ray, origin, axis);
        if (distance == RayUtil.MISS)
            return RayUtil.MISS;

        final var point = new Vector3f(ray.origin()).fma(distance, ray.direction());
        return Math.abs(point.distance(origin) - size) <= size * RING_TOLERANCE ? distance : RayUtil.MISS;
    }

    /** The distance along the ray to the square of the plane handle that the axis is perpendicular to, when the ray crosses the square */
    private float plane(final Ray ray, final Vector3fc origin, final Matrix3fc axes, final int index, final float size) {
        final var normal = axis(axes, index);
        final var distance = RayUtil.intersectPlane(ray, origin, normal);
        if (distance == RayUtil.MISS)
            return RayUtil.MISS;

        final var offset = new Vector3f(ray.origin()).fma(distance, ray.direction()).sub(origin);
        final var u = offset.dot(axis(axes, (index + 1) % 3)) / size;
        final var v = offset.dot(axis(axes, (index + 2) % 3)) / size;

        return u >= PLANE_START && u <= PLANE_END && v >= PLANE_START && v <= PLANE_END ? distance : RayUtil.MISS;
    }

    /**
     * The nearest points of a ray and a segment.
     *
     * @return the distance between the points, and the distance along the ray to its point
     */
    float[] closest(final Ray ray, final Vector3f start, final Vector3f end) {
        final var direction = new Vector3f(end).sub(start);
        final var offset = new Vector3f(ray.origin()).sub(start);

        final var length = direction.dot(direction);
        final var along = ray.direction().dot(direction);
        final var toRay = ray.direction().dot(offset);
        final var toSegment = direction.dot(offset);
        final var denominator = length - along * along;

        final var nearest = denominator > 1e-9f ? Math.max((along * toSegment - toRay * length) / denominator, 0.f) : 0.f;
        final var onSegment = length > 1e-9f ? Math.clamp((along * nearest + toSegment) / length, 0.f, 1.f) : 0.f;
        final var onRay = Math.max(along * onSegment - toRay, 0.f);

        final var pointOnRay = new Vector3f(ray.origin()).fma(onRay, ray.direction());
        final var pointOnSegment = new Vector3f(start).fma(onSegment, direction);

        return new float[]{pointOnRay.distance(pointOnSegment), onRay};
    }

    /** The distance along the axis line of the point that is the nearest to the ray, from the origin; the ray that is parallel to the line gives 0 */
    public float alongAxis(@NonNull final Ray ray, @NonNull final Vector3fc origin, @NonNull final Vector3fc axis) {
        final var offset = new Vector3f(ray.origin()).sub(origin);
        final var along = ray.direction().dot(axis);
        final var toRay = ray.direction().dot(offset);
        final var toAxis = axis.dot(offset);
        final var denominator = 1.f - along * along;

        if (denominator < 1e-6f)
            return 0.f;

        final var onRay = (along * toAxis - toRay) / denominator;
        return toAxis + along * onRay;
    }
}
