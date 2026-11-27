package io.github.trimax.venta.engine.model.common.gizmo;

import lombok.Getter;

/**
 * A handle of the gizmo that can be taken with the mouse. An axis handle is an arrow (move), a ring round the axis (turn) or a cube
 * at the end of the axis (scale). A plane handle is a square that moves the thing in the plane (it is named by the axis it is
 * perpendicular to), the uniform handle is the cube in the middle that scales all the axes.
 */
@Getter
public enum GizmoHandle {
    AxisX(0, false), AxisY(1, false), AxisZ(2, false),
    PlaneYZ(0, true), PlaneXZ(1, true), PlaneXY(2, true),
    Uniform(-1, false);

    /** The axis of the handle: the axis it moves along or turns round, the axis that a plane is perpendicular to; -1 for none */
    private final int axis;

    private final boolean plane;

    GizmoHandle(final int axis, final boolean plane) {
        this.axis = axis;
        this.plane = plane;
    }

    /** The handle of an axis (0 is x) */
    public static GizmoHandle ofAxis(final int axis) {
        return values()[axis];
    }

    /** The handle of the plane that the axis is perpendicular to */
    public static GizmoHandle ofPlane(final int axis) {
        return values()[3 + axis];
    }
}
