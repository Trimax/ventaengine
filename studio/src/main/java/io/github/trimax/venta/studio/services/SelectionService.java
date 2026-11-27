package io.github.trimax.venta.studio.services;

import java.util.ArrayList;

import org.joml.Vector3f;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.model.common.overlay.EditorOverlay;
import io.github.trimax.venta.engine.utils.OverlayUtil;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.OutlineSelectedEvent;
import io.github.trimax.venta.studio.model.event.OutlineSelectionRequestEvent;
import io.github.trimax.venta.studio.model.event.SceneChangedEvent;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import io.github.trimax.venta.studio.model.outline.OutlineItem;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.outline.OutlineNode;
import io.github.trimax.venta.studio.utils.FramingUtil;
import io.github.trimax.venta.studio.utils.OutlineUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ties the hierarchy to the viewport: a click on a thing in the viewport chooses its item in the hierarchy, and the item that is chosen
 * gets its frame in the viewport. The thing of an item is the one that has the same place in the file.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectionService {
    /** A mark has no size, it is looked at from this far (in meters) */
    private static final float MARK_FOCUS_RADIUS = 2.f;

    private final StudioSession session;
    private final Engine engine;
    private final VentaContext context;
    private final SceneApplier applier;

    private OutlineNode outline;
    private OutlineItem selected;

    /** The item that is chosen, null for none */
    public OutlineItem getSelected() {
        return selected;
    }

    /** The hierarchy of the scene that is open, null before one is open */
    public OutlineNode getOutline() {
        return outline;
    }

    @Subscribe
    public void onSceneOpened(final SceneOpenedEvent event) {
        outline = event.outline();
        selected = null;
        overlay().setSelected(null);
    }

    /** The hierarchy was made again: the chosen item is the one in the same place, or the one with the same name for what has no place */
    @Subscribe
    public void onSceneChanged(final SceneChangedEvent event) {
        outline = event.outline();

        if (selected == null)
            return;

        selected = selected.isThing()
                ? OutlineUtil.at(outline, selected.kind(), selected.index())
                : OutlineUtil.items(outline, selected.kind()).stream().filter(item -> item.name().equals(selected.name())).findFirst().orElse(null);
    }

    @Subscribe
    public void onOutlineSelected(final OutlineSelectedEvent event) {
        selected = event.item();

        final var instance = instanceOf(selected);
        overlay().setSelected(instance);

        final var frame = frameOf(instance);
        if (frame != null)
            session.getCamera().getPivot().set(frame.center());
    }

    /**
     * What is under a point of the viewport is chosen, or nothing when there is nothing.
     *
     * @param x the point from 0 (the left edge of the viewport) to 1 (the right one)
     * @param y the point from 0 (the top edge) to 1 (the bottom one)
     */
    public void pick(final float x, final float y) {
        if (outline == null)
            return;

        final var result = engine.pick(x, y);
        EventUtil.post(new OutlineSelectionRequestEvent(result == null ? null : itemOf(result.instance())));
    }

    /** The camera looks at the chosen thing from a distance that holds it */
    public void focusSelection() {
        final var frame = frameOf(instanceOf(selected));
        if (frame != null)
            session.getCamera().focus(frame.center(), frame.radius());
    }

    private EditorOverlay overlay() {
        return session.getOverlay();
    }

    /** The thing of the item in the scene that is open, null for an item that is not a thing (a setting, a category) */
    private AbstractInstance instanceOf(final OutlineItem item) {
        return item == null || !item.isThing() ? null : applier.instanceOf(item.kind(), item.index());
    }

    /** The item of a thing of the scene, null when the hierarchy has none for it */
    private OutlineItem itemOf(final AbstractInstance instance) {
        final var kind = kindOf(instance);
        if (kind == null)
            return null;

        final var index = applier.indexOf(kind, instance);
        return index < 0 ? null : OutlineUtil.at(outline, kind, index);
    }

    private OutlineKind kindOf(final AbstractInstance instance) {
        return switch (instance) {
            case ObjectInstance _ -> OutlineKind.Object;
            case LightInstance _ -> OutlineKind.Light;
            case EmitterInstance _ -> OutlineKind.Emitter;
            case BillboardInstance _ -> OutlineKind.Billboard;
            case SoundSourceInstance _ -> OutlineKind.Sound;
            default -> null;
        };
    }

    /** The place of a thing and how far it reaches: the box of an object, a small ball round a mark */
    private FramingUtil.Frame frameOf(final AbstractInstance instance) {
        if (instance instanceof ObjectInstanceImplementation object) {
            final var model = object.getTransform().getMatrix();
            final var box = object.getBox();

            final var corners = new ArrayList<Vector3f>();
            for (int index = 0; index < 8; index++)
                corners.add(model.transformPosition(new Vector3f((index & 1) == 0 ? box.min().x : box.max().x,
                        (index & 2) == 0 ? box.min().y : box.max().y, (index & 4) == 0 ? box.min().z : box.max().z)));

            return FramingUtil.bounds(corners);
        }

        if (instance == null)
            return null;

        return OverlayUtil.marks(context.getSceneManager().getCurrent()).stream()
                .filter(mark -> mark.instance() == instance)
                .findFirst()
                .map(mark -> new FramingUtil.Frame(new Vector3f(mark.position()), MARK_FOCUS_RADIUS))
                .orElse(null);
    }
}
