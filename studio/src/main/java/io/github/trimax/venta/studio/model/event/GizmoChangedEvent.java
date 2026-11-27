package io.github.trimax.venta.studio.model.event;

import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoSpace;

/**
 * The tool of the viewport or the space of its handles is changed.
 *
 * @param mode the handles that are drawn on the chosen thing; null for the tool that only chooses
 * @param space whether the handles go along the axes of the world or along the axes of the thing
 */
public record GizmoChangedEvent(GizmoMode mode, GizmoSpace space) {
}
