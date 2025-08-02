package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.managers.GizmoManager;
import io.github.trimax.venta.engine.model.view.GizmoView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoManagerImplementation
        extends AbstractManagerImplementation<GizmoManagerImplementation.GizmoEntity, GizmoView>
        implements GizmoManager {
    private final ProgramManagerImplementation programManager;
    private final MeshManagerImplementation meshManager;

    private GizmoEntity origin;

    public GizmoEntity getOrigin() {
        if (origin == null)
            this.origin = create("Origin", GizmoType.Origin);

        return origin;
    }

    public GizmoEntity create(final String name, final GizmoType type) {
        log.debug("Creating gizmo {}", name);

        return store(new GizmoManagerImplementation.GizmoEntity(name,
                programManager.load(ProgramType.Simple.name()),
                meshManager.load(type.getMesh()),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f)));
    }

    @Override
    protected void destroy(final GizmoEntity object) {
        log.debug("Destroying gizmo {} ({})", object.getID(), object.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Gizmo;
    }

    @Getter
    public static final class GizmoEntity extends AbstractEntity implements GizmoView {
        private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
        private final ProgramManagerImplementation.ProgramEntity program;
        private final MeshManagerImplementation.MeshEntity mesh;

        GizmoEntity(final String name,
                    final ProgramManagerImplementation.ProgramEntity program,
                    final MeshManagerImplementation.MeshEntity mesh,
                    final Vector3f position,
                    final Vector3f rotation,
                    final Vector3f scale) {
            super(name);

            this.mesh = mesh;
            this.program = program;

            this.position.set(position);
            this.rotation.set(rotation);
            this.scale.set(scale);
        }
    }
}
