package io.github.trimax.venta.engine.model.common.gizmo;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.utils.GizmoUtil;
import io.github.trimax.venta.engine.utils.LightShapeUtil;
import io.github.trimax.venta.engine.utils.RayUtil;
import lombok.NonNull;

/**
 * A handle of a light that is dragged: how far the light shines, or an angle of its cone. It remembers what the mouse was on when the
 * handle was taken and says what the value is for the place of the mouse now.
 */
public final class LightHandleDrag {
    /** What the steps are with the snapping on: meters and degrees */
    public static final float RANGE_STEP = 0.5f;
    public static final float ANGLE_STEP = 5.f;

    public static final float MINIMAL_RANGE = 0.1f;
    public static final float MINIMAL_ANGLE = 1.f;
    public static final float MAXIMAL_ANGLE = 85.f;

    private final LightShapeUtil.Handle handle;
    private final Vector3f apex;
    private final float start;
    private final float startAlong;

    private LightHandleDrag(final LightShapeUtil.Handle handle, final Vector3fc apex, final float start, final float startAlong) {
        this.handle = handle;
        this.apex = new Vector3f(apex);
        this.start = start;
        this.startAlong = startAlong;
    }

    /**
     * Takes a handle.
     *
     * @param apex    where the light is
     * @param value   the value that the handle has now: the range, or the angle in degrees
     * @param pressed the ray of the point where the mouse was pressed
     */
    public static LightHandleDrag begin(@NonNull final LightShapeUtil.Handle handle, @NonNull final Vector3fc apex, final float value, @NonNull final Ray pressed) {
        final var along = handle.kind() == LightHandle.Range ? GizmoUtil.alongAxis(pressed, apex, handle.axis()) : 0.f;
        return new LightHandleDrag(handle, apex, value, along);
    }

    /**
     * The value for the mouse at the ray.
     *
     * @param snap whether the value is in whole steps of half a meter or of five degrees
     * @return the range in meters or the angle in degrees, the value that the drag started with when the ray doesn't give a new one
     */
    public float update(@NonNull final Ray ray, final boolean snap) {
        if (handle.kind() == LightHandle.Range) {
            final var range = Math.max(MINIMAL_RANGE, start + GizmoUtil.alongAxis(ray, apex, handle.axis()) - startAlong);
            return snap ? Math.max(RANGE_STEP, Math.round(range / RANGE_STEP) * RANGE_STEP) : range;
        }

        // The angle is measured in the plane of the axis of the cone and the handle, which the mouse is carried to
        final var normal = new Vector3f(handle.direction()).cross(handle.axis());
        final var distance = RayUtil.intersectPlane(ray, apex, normal);
        if (distance == RayUtil.MISS)
            return start;

        final var offset = new Vector3f(ray.origin()).fma(distance, ray.direction()).sub(apex);
        final var angle = (float) Math.toDegrees(Math.atan2(offset.dot(handle.axis()), offset.dot(handle.direction())));

        final var clamped = Math.clamp(snap ? Math.round(angle / ANGLE_STEP) * ANGLE_STEP : angle, MINIMAL_ANGLE, MAXIMAL_ANGLE);
        return clamped;
    }
}
