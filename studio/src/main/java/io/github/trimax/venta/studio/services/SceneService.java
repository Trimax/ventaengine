package io.github.trimax.venta.studio.services;

import java.util.EnumSet;
import java.util.Optional;

import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.WireframeTarget;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import io.github.trimax.venta.studio.model.event.WireframeChangedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.utils.FramingUtil;
import io.github.trimax.venta.studio.utils.OutlineUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Opens the scenes of the project in the viewport: the scene that was open is replaced, and the camera is put round the new one.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneService {
    private final StudioSession session;
    private final VentaContext context;
    private final ResourceService resources;

    /**
     * Opens a scene of the project.
     *
     * @param name the file of the scene inside the folder of the scenes, like {@code beach.json}
     * @return whether the scene is open; when it isn't, the scene that was open stays
     */
    public boolean open(@NonNull final String name) {
        try {
            final var file = resources.getAsObject("/scenes/" + name, SceneDTO.class);
            if (file == null)
                throw new IllegalStateException("The scene file is empty");

            final var manager = context.getSceneManager();
            final var previous = manager.getCurrent();
            final var scene = manager.create(name, context.getSceneRepository().get(name));
            manager.setCurrent(scene);
            if (previous != null)
                manager.delete(previous);

            session.setScene(name);
            applyWireframe();

            final var frame = FramingUtil.frame(OutlineUtil.positions(file));
            session.getCamera().frame(frame == null ? new FramingUtil.Frame(new Vector3f(0.f, 2.f, 0.f), FramingUtil.MINIMAL_RADIUS) : frame);

            EventUtil.post(new SceneOpenedEvent(name, file, OutlineUtil.build(name, file)));
            EventUtil.post(new StatusEvent("Scene %s is open", name));
            return true;
        } catch (final RuntimeException exception) {
            log.error("Can't open the scene {}", name, exception);
            EventUtil.post(new StatusEvent("Can't open the scene %s: %s", name, Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().getSimpleName())));
            return false;
        }
    }

    /**
     * The things of a kind are drawn as lines, or as they are.
     */
    public void setWireframe(final WireframeTarget target, final boolean wireframe) {
        if (wireframe)
            session.getWireframe().add(target);
        else
            session.getWireframe().remove(target);

        applyWireframe();
        EventUtil.post(new WireframeChangedEvent());
    }

    /**
     * Every kind of things is drawn as lines, or none.
     */
    public void setWireframeEverywhere(final boolean wireframe) {
        session.getWireframe().clear();
        if (wireframe)
            session.getWireframe().addAll(EnumSet.allOf(WireframeTarget.class));

        applyWireframe();
        EventUtil.post(new WireframeChangedEvent());
    }

    private void applyWireframe() {
        final var scene = context.getSceneManager().getCurrent();
        if (scene == null)
            return;

        scene.getObjects().forEach(object -> object.setDrawMode(modeOf(WireframeTarget.Objects)));
        scene.getTerrainSurfaces().forEach(surface -> surface.setDrawMode(modeOf(WireframeTarget.Terrain)));
        scene.getWaterSurfaces().forEach(surface -> surface.setDrawMode(modeOf(WireframeTarget.Water)));
        scene.getEmitters().forEach(emitter -> emitter.setDrawMode(modeOf(WireframeTarget.Emitters)));
        scene.getBillboards().forEach(billboard -> billboard.setDrawMode(modeOf(WireframeTarget.Billboards)));
    }

    private DrawMode modeOf(final WireframeTarget target) {
        return session.getWireframe().contains(target) ? DrawMode.Edge : DrawMode.Polygon;
    }
}
