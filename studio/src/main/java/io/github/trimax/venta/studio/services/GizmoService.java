package io.github.trimax.venta.studio.services;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoDrag;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoHandle;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoSpace;
import io.github.trimax.venta.engine.model.common.gizmo.LightHandleDrag;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.utils.LightShapeUtil;
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
    private final SceneApplier applier;

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
    private Vector3f startDirection;

    /** The handle of the range or of the cone of the chosen light that is taken, with what it changes: the file of the light and the field of it */
    private LightHandleDrag lightDrag;
    private LightShapeUtil.Handle lightTaken;
    private String lightPath;

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
        if (drag != null || lightDrag != null)
            return;

        final var overlay = session.getOverlay();
        final var light = lightHandleAt(x, y);
        overlay.setLightHandle(light == null ? -1 : light.index());
        if (light != null) {
            overlay.setGizmoHandle(null);
            return;
        }

        if (!isShown())
            return;

        final var handle = hit(x, y);
        if (handle != overlay.getGizmoHandle())
            overlay.setGizmoHandle(handle);
    }

    /** The mouse is down on a point of the viewport; returns whether it took a handle (then it is not a click that chooses) */
    public boolean press(final float x, final float y) {
        if (pressLightHandle(x, y))
            return true;

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
        startDirection = EntryUtil.getVector(entry, EntryUtil.DIRECTION, new Vector3f(0.f, -1.f, 0.f));

        dragMode = mode;
        dragKey = "gizmo-" + ++dragCounter;
        drag = GizmoDrag.begin(mode, handle, overlay.getGizmoOrigin(), overlay.getGizmoAxes(), engine.rayThrough(x, y), session.getCamera().getPosition());
        edits.beginDrag();
        return true;
    }

    /** The mouse moved with a handle taken: the thing follows it; with the snapping the result is in whole steps */
    public void drag(final float x, final float y, final boolean snap) {
        if (lightDrag != null) {
            dragLightHandle(x, y, snap);
            return;
        }

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
                    if (kind == OutlineKind.Light) {
                        // A spot light has a direction and no angles: the direction is turned
                        EntryUtil.setVector(entry, EntryUtil.DIRECTION, result.rotation().transform(new Vector3f(startDirection)).normalize());
                        return;
                    }

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
        if (lightDrag != null) {
            lightDrag = null;
            lightTaken = null;
            edits.endDrag();
            session.getOverlay().setLightHandle(-1);
            return;
        }

        if (drag == null)
            return;

        drag = null;
        edits.endDrag();
        session.getOverlay().setGizmoHandle(null);
    }

    public boolean isDragging() {
        return drag != null || lightDrag != null;
    }

    /** The chosen light, null when what is chosen is not a light */
    private LightInstance chosenLight() {
        return kind == OutlineKind.Light && index >= 0 ? applier.instanceOf(OutlineKind.Light, index) instanceof LightInstance light ? light : null : null;
    }

    /** The handle of the range or the cone of the chosen light under a point of the viewport, null when there is none */
    private LightShapeUtil.Handle lightHandleAt(final float x, final float y) {
        final var light = chosenLight();
        return light == null ? null : LightShapeUtil.hit(LightShapeUtil.handles(light), engine.rayThrough(x, y), session.getCamera().getPosition());
    }

    private boolean pressLightHandle(final float x, final float y) {
        final var light = chosenLight();
        final var handle = lightHandleAt(x, y);
        final var entry = entry();
        if (light == null || handle == null || entry == null || !entry.has("light"))
            return false;

        final var value = switch (handle.kind()) {
            case Range -> handle.point().distance(light.getPosition());
            case Outer -> light.getCone().getOuterAngle();
            case Inner -> light.getCone().getInnerAngle();
        };

        lightPath = "lights/" + entry.get("light").getAsString();
        lightTaken = handle;
        lightDrag = LightHandleDrag.begin(handle, light.getPosition(), value, engine.rayThrough(x, y));
        dragKey = "light-handle-" + ++dragCounter;
        session.getOverlay().setLightHandle(handle.index());
        edits.beginDrag();
        return true;
    }

    /** The mouse moved with a handle of the light taken: the file of the light follows it, so every light of the file does */
    private void dragLightHandle(final float x, final float y, final boolean snap) {
        final var light = chosenLight();
        if (light == null)
            return;

        final var value = Math.round(lightDrag.update(engine.rayThrough(x, y), snap) * 100.f) / 100.0;
        switch (lightTaken.kind()) {
            case Range -> edits.editResource(lightPath, "Range of", dragKey, file -> file.addProperty("range", value));
            case Outer -> edits.editResource(lightPath, "Outer angle of", dragKey, file -> file.addProperty("outerAngle", Math.max(value, light.getCone().getInnerAngle())));
            case Inner -> edits.editResource(lightPath, "Inner angle of", dragKey, file -> file.addProperty("innerAngle", Math.min(value, light.getCone().getOuterAngle())));
            default -> {
            }
        }
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
        final var entry = entry();

        if (lightDrag == null)
            overlay.setLightHandle(-1);

        if (mode == null || entry == null || !supports(kind, mode, entry)) {
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
    private JsonObject entry() {
        final var document = edits.getDocument();
        if (document == null || kind == null || index < 0 || index >= document.count(kind))
            return null;

        return document.getEntry(kind, index);
    }

    private static boolean hasAngles(final OutlineKind kind) {
        return kind == OutlineKind.Object || kind == OutlineKind.Terrain || kind == OutlineKind.Water;
    }

    /**
     * What can be turned and scaled has its angles and scale in the file, a light that shines in a direction can be turned; everything with
     * a place can be moved.
     */
    private static boolean supports(final OutlineKind kind, final GizmoMode mode, final JsonObject entry) {
        if (kind == null || mode == GizmoMode.Translate)
            return kind != null;

        return hasAngles(kind) || mode == GizmoMode.Rotate && kind == OutlineKind.Light && EntryUtil.has(entry, EntryUtil.DIRECTION);
    }
}
