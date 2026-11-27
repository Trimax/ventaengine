package io.github.trimax.venta.engine.utils;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.gizmo.GizmoLine;
import io.github.trimax.venta.engine.model.common.gizmo.LightHandle;
import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.model.common.shared.Cone;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The lines that show how far a light shines, for an editor: three circles of the range round a point light, a cone of the angles and of
 * the range for a spot light.
 */
@UtilityClass
public final class LightShapeUtil {
    public static final float[] COLOR = {1.f, 0.86f, 0.35f};
    public static final float[] INNER_COLOR = {0.75f, 0.64f, 0.26f};

    /** How long the cone of a spot light is drawn when the light has no range */
    public static final float DEFAULT_LENGTH = 5.f;

    public static final int SEGMENTS = 48;
    public static final int EDGES = 4;

    /** The lines of the light, none for a light that has no shape to show (a point light without a range) */
    public List<GizmoLine> lines(@NonNull final LightInstance light) {
        return lines(light.getPosition(), light.getRange(), light.getDirection(), light.getCone());
    }

    /**
     * The lines of a light.
     *
     * @param range     how far the light shines, 0 for a light that has no end
     * @param direction where a spot light shines
     * @param cone      the angles of a spot light, null for a point light
     */
    public List<GizmoLine> lines(@NonNull final Vector3fc position, final float range, @NonNull final Vector3fc direction, final Cone cone) {
        final var lines = new ArrayList<GizmoLine>();

        if (cone == null) {
            if (range > 0.f)
                for (int axis = 0; axis < 3; axis++)
                    circle(lines, position, axis(axis, 0), axis(axis, 1), range, COLOR);

            return lines;
        }

        final var length = range > 0.f ? range : DEFAULT_LENGTH;
        final var unit = new Vector3f(direction).normalize();
        cone(lines, position, unit, length, cone.getOuterAngle(), COLOR, true);
        cone(lines, position, unit, length, cone.getInnerAngle(), INNER_COLOR, false);

        return lines;
    }

    /**
     * A handle of a light: a small cube at a point of its shape that is dragged.
     *
     * @param index     the place of the handle in the list, so a handle is told from the others
     * @param kind      what dragging it changes
     * @param point     where it is
     * @param axis      the line it is dragged along (the range), or the unit vector from the axis of the cone to it (the angles)
     * @param direction the axis of the cone, a unit vector (zero for a point light)
     */
    public record Handle(int index, LightHandle kind, Vector3f point, Vector3f axis, Vector3f direction) {
    }

    /** The color of the handle that the mouse is over */
    public static final float[] HIGHLIGHT_COLOR = {1.f, 1.f, 0.55f};

    /** The size of a handle on the screen as a part of the size of the gizmo, and how far from it the mouse still takes it */
    private static final float HANDLE_SIZE = 0.12f;
    private static final float HANDLE_REACH = 2.4f;

    /**
     * The handles of the light, none for a light that has no shape (see {@link #lines(LightInstance)}).
     */
    public List<Handle> handles(@NonNull final LightInstance light) {
        return handles(light.getPosition(), light.getRange(), light.getDirection(), light.getCone());
    }

    /**
     * The handles of a light: a point light has one for the range on every axis, a spot light has one at the end of its axis and one on
     * the edge of every cone, the outer and the inner one.
     */
    public List<Handle> handles(@NonNull final Vector3fc position, final float range, @NonNull final Vector3fc direction, final Cone cone) {
        final var handles = new ArrayList<Handle>();

        if (cone == null) {
            if (range > 0.f)
                for (int axis = 0; axis < 3; axis++) {
                    final var unit = new Vector3f();
                    unit.setComponent(axis, 1.f);
                    handles.add(new Handle(handles.size(), LightHandle.Range, new Vector3f(position).fma(range, unit), unit, new Vector3f()));
                }

            return handles;
        }

        final var length = range > 0.f ? range : DEFAULT_LENGTH;
        final var unit = new Vector3f(direction).normalize();
        final var u = perpendicular(unit);

        handles.add(new Handle(0, LightHandle.Range, new Vector3f(position).fma(length, unit), unit, unit));
        handles.add(new Handle(1, LightHandle.Outer, edge(position, unit, u, length, cone.getOuterAngle()), u, unit));
        handles.add(new Handle(2, LightHandle.Inner, edge(position, unit, u, length, cone.getInnerAngle()), u, unit));

        return handles;
    }

    /** The cubes of the handles, the one of the index is lit; the cubes are of the same size on the screen whatever the distance */
    public List<GizmoLine> handleLines(@NonNull final List<Handle> handles, @NonNull final Vector3fc camera, final int highlighted) {
        final var lines = new ArrayList<GizmoLine>();
        for (final var handle : handles) {
            final var half = GizmoUtil.size(handle.point(), camera) * HANDLE_SIZE;
            final var color = handle.index() == highlighted ? HIGHLIGHT_COLOR : COLOR;
            final var corners = new Vector3f[8];
            for (int corner = 0; corner < 8; corner++)
                corners[corner] = new Vector3f(handle.point()).add((corner & 1) == 0 ? -half : half, (corner & 2) == 0 ? -half : half, (corner & 4) == 0 ? -half : half);

            for (int corner = 0; corner < 8; corner++)
                for (int bit = 1; bit <= 4; bit <<= 1)
                    if ((corner & bit) == 0)
                        lines.add(new GizmoLine(corners[corner], corners[corner | bit], color));
        }

        return lines;
    }

    /** The handle that the ray takes, the nearest one; null when it takes none */
    public Handle hit(@NonNull final List<Handle> handles, @NonNull final Ray ray, @NonNull final Vector3fc camera) {
        Handle best = null;
        var nearest = Float.MAX_VALUE;

        for (final var handle : handles) {
            final var distance = RayUtil.intersectSphere(ray, handle.point(), GizmoUtil.size(handle.point(), camera) * HANDLE_SIZE * HANDLE_REACH);
            if (distance != RayUtil.MISS && distance < nearest) {
                best = handle;
                nearest = distance;
            }
        }

        return best;
    }

    /** The point of the edge of the cone: on the sphere of the length round the apex, at the angle from the axis */
    private Vector3f edge(final Vector3fc apex, final Vector3fc direction, final Vector3fc u, final float length, final float angle) {
        final var radians = (float) Math.toRadians(angle);
        return new Vector3f(apex).fma(length * (float) Math.cos(radians), direction).fma(length * (float) Math.sin(radians), u);
    }

    /** The axis of the world by its index, the next one after it for the offset 0 and the one after that for 1 (so a circle goes round the missing one) */
    private Vector3f axis(final int index, final int offset) {
        final var vector = new Vector3f();
        vector.setComponent((index + 1 + offset) % 3, 1.f);
        return vector;
    }

    private void circle(final List<GizmoLine> lines, final Vector3fc center, final Vector3fc u, final Vector3fc v, final float radius, final float[] color) {
        Vector3f previous = null;
        for (int step = 0; step <= SEGMENTS; step++) {
            final var angle = (float) (2.0 * Math.PI * step / SEGMENTS);
            final var point = new Vector3f(center).fma((float) Math.cos(angle) * radius, u).fma((float) Math.sin(angle) * radius, v);

            if (previous != null)
                lines.add(new GizmoLine(previous, point, color));

            previous = point;
        }
    }

    /**
     * A cone of a half angle: its edges are as long as the length, and its base is the circle where they end.
     *
     * @param axis   whether the line along the direction and the edges are drawn (the inner cone has only its circle)
     */
    private void cone(final List<GizmoLine> lines, final Vector3fc apex, final Vector3fc direction, final float length, final float angle, final float[] color,
                      final boolean axis) {
        final var u = perpendicular(direction);
        final var v = new Vector3f(direction).cross(u).normalize();

        final var radians = (float) Math.toRadians(angle);
        final var base = new Vector3f(apex).fma(length * (float) Math.cos(radians), direction);
        final var radius = length * (float) Math.sin(radians);

        circle(lines, base, u, v, radius, color);

        if (!axis)
            return;

        lines.add(new GizmoLine(new Vector3f(apex), new Vector3f(apex).fma(length, direction), color));
        for (int edge = 0; edge < EDGES; edge++) {
            final var around = (float) (Math.PI * 2.0 * edge / EDGES);
            final var end = new Vector3f(base).fma((float) Math.cos(around) * radius, u).fma((float) Math.sin(around) * radius, v);
            lines.add(new GizmoLine(new Vector3f(apex), end, color));
        }
    }

    /** A unit vector that is at a right angle to the direction */
    private Vector3f perpendicular(final Vector3fc direction) {
        final var helper = Math.abs(direction.y()) < 0.9f ? new Vector3f(0.f, 1.f, 0.f) : new Vector3f(1.f, 0.f, 0.f);
        return new Vector3f(direction).cross(helper).normalize();
    }
}
