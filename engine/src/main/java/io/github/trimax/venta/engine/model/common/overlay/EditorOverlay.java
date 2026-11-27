package io.github.trimax.venta.engine.model.common.overlay;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.gizmo.GizmoHandle;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import lombok.Getter;
import lombok.Setter;

/**
 * What an editor draws over the finished image of the scene: the grid of the ground, the marks of the things that have no shape
 * (lights, emitters, sounds), and the frame of the thing that is chosen. A game has none of it.
 */
@Getter
@Setter
public final class EditorOverlay {
    /** Whether the grid of the ground is drawn */
    private boolean grid = true;

    /** Whether the marks of the lights, the emitters, the billboards and the sounds are drawn */
    private boolean marks = true;

    /** The thing that is chosen, it gets a frame; null for none */
    private AbstractInstance selected;

    /** What the handles of the gizmo do: move, turn or scale; null hides the gizmo */
    private GizmoMode gizmoMode;

    /** Where the gizmo is: the place of the thing that is chosen */
    private final Vector3f gizmoOrigin = new Vector3f();

    /** The axes of the gizmo as the columns: the axes of the world, or the axes of the thing that is chosen */
    private final Matrix3f gizmoAxes = new Matrix3f();

    /** Whether the chosen light has handles for its range and its cone */
    private boolean lightHandles = true;

    /** The place in the list of handles of the light of the one that the mouse is over or that is dragged, it is drawn lit; -1 for none */
    private int lightHandle = -1;

    /** The handle that the mouse is over or that is dragged, it is drawn in another color; null for none */
    private GizmoHandle gizmoHandle;
}
