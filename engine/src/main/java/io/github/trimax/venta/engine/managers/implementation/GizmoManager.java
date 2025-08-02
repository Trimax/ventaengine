package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.model.view.GizmoView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoManager extends AbstractManager<GizmoManager.GizmoEntity, GizmoView> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final MeshManager.MeshAccessor meshAccessor;
    private final ProgramManager programManager;
    private final MeshManager meshManager;

    private GizmoView origin;

    public GizmoView getOrigin() {
        if (origin == null)
            this.origin = create("Origin", GizmoType.Origin);

        return origin;
    }

    public GizmoView create(final String name, final GizmoType type) {
        log.debug("Creating gizmo {}", name);

        return store(new GizmoManager.GizmoEntity(name,
                programAccessor.get(programManager.load(ProgramType.Simple.name())),
                meshAccessor.get(meshManager.load(type.getMesh())),
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
        private final ProgramManager.ProgramEntity program;
        private final MeshManager.MeshEntity mesh;

        GizmoEntity(final String name,
                    final ProgramManager.ProgramEntity program,
                    final MeshManager.MeshEntity mesh,
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

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class GizmoAccessor extends AbstractAccessor {}
}
