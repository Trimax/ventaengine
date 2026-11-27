package io.github.trimax.venta.studio.services;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoDrag;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoHandle;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoSpace;
import io.github.trimax.venta.engine.utils.GizmoUtil;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.GizmoChangedEvent;
import io.github.trimax.venta.studio.model.event.OutlineSelectedEvent;
import io.github.trimax.venta.studio.model.event.SceneChangedEvent;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.utils.EntryUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The handles on the chosen thing (move, turn, scale). The engine draws them and says which of them is under the mouse and what a drag
 * of one does; this one keeps the tool and the space, puts the handles where the thing is, and writes the result of a drag to the scene
 * as a command, so a drag is one step of the history.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoService {
    private final StudioSession session;
    private final SceneEditService edits;
    private final Engine engine;

    /** The handles that are drawn; null for the tool that only chooses */
    @Getter
    private GizmoMode mode = GizmoMode.Translate;

    @Getter
    private GizmoSpace space = GizmoSpace.Global;

    private OutlineKind kind;
    private int index = -1;

    private GizmoDrag drag;
    private GizmoMode dragMode;
    private int dragCounter;
    private String dragKey;
    private Vector3f startPosition;
    private Vector3f startAngles;
    private Vector3f startScale;

    /** The tool is chosen: null is the one that only chooses */
    public void setMode(final GizmoMode mode) {
        this.mode = mode;
        changed();
    }

    public void setSpace(@NonNull final GizmoSpace space) {
        this.space = space;
        changed();
    }

    @Subscribe
    public void onSceneOpened(final SceneOpenedEvent event) {
        kind = null;
        index = -1;
        refresh();
    }

    @Subscribe
    public void onOutlineSelected(final OutlineSelectedEvent event) {
        final var item = event.item();
        kind = item != null && item.isThing() ? item.kind() : null;
        index = kind == null ? -1 : item.index();
        refresh();
    }

    @Subscribe
    public void onSceneChanged(final SceneChangedEvent event) {
        refresh();
    }

    /** The mouse is over a point of the viewport (from 0 to 1): the handle under it is lit */
    public void hover(final float x, final float y) {
        if (drag != null || !isShown())
            return;

        final var overlay = session.getOverlay();
        final var handle = hit(x, y);
        if (handle != overlay.getGizmoHandle())
            overlay.setGizmoHandle(handle);
    }

    /** The mouse is down on a point of the viewport; returns whether it took a handle (then it is not a click that chooses) */
    public boolean press(final float x, final float y) {
        if (!isShown())
            return false;

        final var handle = hit(x, y);
        final var entry = entry();
        if (handle == null || entry == null)
            return false;

        final var overlay = session.getOverlay();
        overlay.setGizmoHandle(handle);

        startPosition = EntryUtil.getVector(entry, EntryUtil.POSITION, new Vector3f());
        startAngles = EntryUtil.getVector(entry, EntryUtil.ANGLES, new Vector3f());
        startScale = EntryUtil.getVector(entry, EntryUtil.SCALE, new Vector3f(1.f));

        dragMode = mode;
        dragKey = "gizmo-" + ++dragCounter;
        drag = GizmoDrag.begin(mode, handle, overlay.getGizmoOrigin(), overlay.getGizmoAxes(), engine.rayThrough(x, y), session.getCamera().getPosition());
        edits.beginDrag();
        return true;
    }

    /** The mouse moved with a handle taken: the thing follows it; with the snapping the result is in whole steps */
    public void drag(final float x, final float y, final boolean snap) {
        if (drag == null || kind == null)
            return;

        final var result = drag.update(engine.rayThrough(x, y), snap);
        final var label = switch (dragMode) {
            case Translate -> "Move";
            case Rotate -> "Rotate";
            case Scale -> "Scale";
            default -> "Change";
        };

        edits.edit(kind, index, label, dragKey, entry -> {
            switch (dragMode) {
                case Translate -> EntryUtil.setVector(entry, EntryUtil.POSITION, new Vector3f(startPosition).add(result.translation()));
                case Rotate -> {
                    final var turned = new Matrix3f(result.rotation()).mul(new Matrix3f().rotationXYZ(startAngles.x, startAngles.y, startAngles.z));
                    EntryUtil.setVector(entry, EntryUtil.ANGLES, turned.getEulerAnglesXYZ(new Vector3f()));
                }
                case Scale -> EntryUtil.setVector(entry, EntryUtil.SCALE, new Vector3f(startScale).mul(result.scale()));
                default -> {
                }
            }
        });
    }

    /** The mouse is up: the drag is done and is one step of the history */
    public void release() {
        if (drag == null)
            return;

        drag = null;
        edits.endDrag();
        session.getOverlay().setGizmoHandle(null);
    }

    public boolean isDragging() {
        return drag != null;
    }

    private GizmoHandle hit(final float x, final float y) {
        final var overlay = session.getOverlay();
        return GizmoUtil.hit(mode, overlay.getGizmoOrigin(), overlay.getGizmoAxes(), engine.rayThrough(x, y), session.getCamera().getPosition());
    }

    private boolean isShown() {
        return session.getOverlay().getGizmoMode() != null;
    }

    private void changed() {
        refresh();
        EventUtil.post(new GizmoChangedEvent(mode, space));
    }

    /** Puts the handles on the chosen thing (or takes them away): they follow the thing while it is dragged, but keep their axes */
    private void refresh() {
        final var overlay = session.getOverlay();
        final var entry = mode == null || !supports(kind, mode) ? null : entry();

        if (entry == null) {
            overlay.setGizmoMode(null);
            overlay.setGizmoHandle(null);
            return;
        }

        overlay.setGizmoMode(mode);
        overlay.getGizmoOrigin().set(EntryUtil.getVector(entry, EntryUtil.POSITION, new Vector3f()));

        if (drag != null)
            return;

        // The scale goes along the axes of the thing whatever the space is
        final var local = space == GizmoSpace.Local || mode == GizmoMode.Scale;
        if (local && hasAngles(kind)) {
            final var angles = EntryUtil.getVector(entry, EntryUtil.ANGLES, new Vector3f());
            overlay.getGizmoAxes().rotationXYZ(angles.x, angles.y, angles.z);
        } else {
            overlay.getGizmoAxes().identity();
        }
    }

    /** The chosen thing in the file, null when nothing is chosen or the thing is gone */
    private com.google.gson.JsonObject entry() {
        final var document = edits.getDocument();
        if (document == null || kind == null || index < 0 || index >= document.count(kind))
            return null;

        return document.getEntry(kind, index);
    }

    private static boolean hasAngles(final OutlineKind kind) {
        return kind == OutlineKind.Object || kind == OutlineKind.Terrain || kind == OutlineKind.Water;
    }

    /** What can be turned and scaled has its angles and scale in the file; everything with a place can be moved */
    private static boolean supports(final OutlineKind kind, final GizmoMode mode) {
        return kind != null && (mode == GizmoMode.Translate || hasAngles(kind));
    }
}
