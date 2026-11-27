package io.github.trimax.venta.engine.model.common.gizmo;

import org.joml.Vector3f;

/**
 * A line of the picture of the gizmo.
 *
 * @param start where it starts
 * @param end   where it ends
 * @param color the red, green and blue of it
 */
public record GizmoLine(Vector3f start, Vector3f end, float[] color) {
}
