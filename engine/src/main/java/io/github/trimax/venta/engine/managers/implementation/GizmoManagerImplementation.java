package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.managers.GizmoManager;
import io.github.trimax.venta.engine.model.entity.GizmoInstance;
import io.github.trimax.venta.engine.model.view.GizmoView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoManagerImplementation
        extends AbstractManagerImplementation<GizmoInstance, GizmoView>
        implements GizmoManager {
    private final ProgramManagerImplementation programManager;
    private final MeshManagerImplementation meshManager;

    private GizmoInstance origin;

    public GizmoInstance getOrigin() {
        if (origin == null)
            this.origin = create("Origin", GizmoType.Origin);

        return origin;
    }

    public GizmoInstance create(final String name, final GizmoType type) {
        log.debug("Creating gizmo {}", name);

        return store(new GizmoInstance(name,
                programManager.load(ProgramType.Simple.name()),
                meshManager.load(type.getMesh()),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f)));
    }

    @Override
    protected void destroy(final GizmoInstance object) {
        log.debug("Destroying gizmo {} ({})", object.getID(), object.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
