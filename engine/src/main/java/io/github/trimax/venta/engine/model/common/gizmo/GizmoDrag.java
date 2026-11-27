package io.github.trimax.venta.engine.model.common.gizmo;

import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.utils.GizmoUtil;
import io.github.trimax.venta.engine.utils.RayUtil;
import lombok.NonNull;

/**
 * A handle of the gizmo that is taken with the mouse and is dragged. It remembers what the mouse was on when the handle was taken, and
 * says what the mouse has done since then: how far the thing has moved, how much it has turned, by how much it has grown. All of it
 * is from the start of the drag, not from the last step, so it doesn't pile up errors.
 */
public final class GizmoDrag {
    /** What a drag does to a thing, from the start of it: the translation in the world, the rotation in the world, the factors along the axes of the thing */
    public record Result(Vector3f translation, Matrix3f rotation, Vector3f scale) {
        public static Result none() {
            return new Result(new Vector3f(), new Matrix3f(), new Vector3f(1.f));
        }
    }

    /** What the steps are with the snapping on: meters, radians and a part of the size */
    public static final float TRANSLATE_STEP = 0.25f;
    public static final float ROTATE_STEP = (float) Math.toRadians(15.0);
    public static final float SCALE_STEP = 0.1f;

    private static final float MINIMAL_FACTOR = 0.01f;

    private final GizmoMode mode;
    private final GizmoHandle handle;
    private final Vector3f origin;
    private final Matrix3f axes;
    private final Vector3f facing;

    private float startAlong;
    private float startAngle;
    private float startDistance;
    private Vector3f startPoint;

    private GizmoDrag(final GizmoMode mode, final GizmoHandle handle, final Vector3fc origin, final Matrix3fc axes, final Vector3fc camera) {
        this.mode = mode;
        this.handle = handle;
        this.origin = new Vector3f(origin);
        this.axes = new Matrix3f(axes);
        this.facing = new Vector3f(camera).sub(origin).normalize();
    }

    /**
     * Takes a handle.
     *
     * @param pressed the ray of the point where the mouse was pressed
     * @param camera  where the camera is
     */
    public static GizmoDrag begin(@NonNull final GizmoMode mode, @NonNull final GizmoHandle handle, @NonNull final Vector3fc origin, @NonNull final Matrix3fc axes,
                                  @NonNull final Ray pressed, @NonNull final Vector3fc camera) {
        final var drag = new GizmoDrag(mode, handle, origin, axes, camera);
        drag.measure(pressed, true);

        return drag;
    }

    /**
     * What the drag has done when the mouse is at the point.
     *
     * @param snap whether the results are round: whole steps of a quarter of a meter, of 15 degrees, of a tenth
     */
    public Result update(@NonNull final Ray ray, final boolean snap) {
        final var result = Result.none();

        switch (mode) {
            case Translate -> translate(ray, snap, result);
            case Rotate -> rotate(ray, snap, result);
            case Scale -> scale(ray, snap, result);
            default -> {
            }
        }

        return result;
    }

    private void translate(final Ray ray, final boolean snap, final Result result) {
        if (handle.isPlane()) {
            final var point = pointOnPlane(ray);
            if (point == null)
                return;

            final var delta = new Vector3f(point).sub(startPoint);
            if (snap) {
                final var u = GizmoUtil.axis(axes, (handle.getAxis() + 1) % 3);
                final var v = GizmoUtil.axis(axes, (handle.getAxis() + 2) % 3);

                delta.set(new Vector3f(u).mul(round(delta.dot(u), TRANSLATE_STEP)).add(new Vector3f(v).mul(round(delta.dot(v), TRANSLATE_STEP))));
            }

            result.translation().set(delta);
            return;
        }

        final var axis = GizmoUtil.axis(axes, handle.getAxis());
        final var moved = GizmoUtil.alongAxis(ray, origin, axis) - startAlong;

        result.translation().set(axis).mul(snap ? round(moved, TRANSLATE_STEP) : moved);
    }

    private void rotate(final Ray ray, final boolean snap, final Result result) {
        final var point = pointOnPlane(ray);
        if (point == null)
            return;

        var turned = wrap(angleOf(point) - startAngle);
        if (snap)
            turned = round(turned, ROTATE_STEP);

        result.rotation().rotation(turned, GizmoUtil.axis(axes, handle.getAxis()));
    }

    private void scale(final Ray ray, final boolean snap, final Result result) {
        var factor = 1.f;

        if (handle == GizmoHandle.Uniform) {
            final var point = pointOnPlane(ray);
            if (point != null && startDistance > 1e-6f)
                factor = point.distance(origin) / startDistance;
        } else if (Math.abs(startAlong) > 1e-6f) {
            factor = GizmoUtil.alongAxis(ray, origin, GizmoUtil.axis(axes, handle.getAxis())) / startAlong;
        }

        factor = Math.max(snap ? round(factor, SCALE_STEP) : factor, snap ? SCALE_STEP : MINIMAL_FACTOR);

        if (handle == GizmoHandle.Uniform)
            result.scale().set(factor);
        else
            result.scale().setComponent(handle.getAxis(), factor);
    }

    /** What the mouse is on at the start of the drag */
    private void measure(final Ray ray, final boolean start) {
        if (handle.isPlane() || mode == GizmoMode.Rotate || handle == GizmoHandle.Uniform) {
            startPoint = pointOnPlane(ray);
            if (startPoint == null)
                startPoint = new Vector3f(origin);

            startAngle = mode == GizmoMode.Rotate ? angleOf(startPoint) : 0.f;
            startDistance = startPoint.distance(origin);
        } else {
            startAlong = GizmoUtil.alongAxis(ray, origin, GizmoUtil.axis(axes, handle.getAxis()));
        }
    }

    /** The point of the plane of the handle that the ray crosses: the plane of the ring, the plane handle, the plane facing the camera; null when it doesn't */
    private Vector3f pointOnPlane(final Ray ray) {
        final var normal = handle == GizmoHandle.Uniform ? facing : GizmoUtil.axis(axes, handle.getAxis());
        final var distance = RayUtil.intersectPlane(ray, origin, normal);

        return distance == RayUtil.MISS ? null : new Vector3f(ray.origin()).fma(distance, ray.direction());
    }

    /** The angle of a point of the plane of the ring, round the axis of it: the axes after it are the zero and the quarter */
    private float angleOf(final Vector3fc point) {
        final var offset = new Vector3f(point).sub(origin);
        final var u = GizmoUtil.axis(axes, (handle.getAxis() + 1) % 3);
        final var v = GizmoUtil.axis(axes, (handle.getAxis() + 2) % 3);

        return (float) Math.atan2(offset.dot(v), offset.dot(u));
    }

    /** An angle in a half circle each way: the way that is shorter */
    private float wrap(final float angle) {
        final var full = (float) (2.0 * Math.PI);
        final var wrapped = angle % full;

        if (wrapped > Math.PI)
            return wrapped - full;

        return wrapped < -Math.PI ? wrapped + full : wrapped;
    }

    private float round(final float value, final float step) {
        return Math.round(value / step) * step;
    }
}
