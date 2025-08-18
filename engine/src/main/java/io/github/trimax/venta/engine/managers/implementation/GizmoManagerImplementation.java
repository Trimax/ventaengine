package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.managers.GizmoManager;
import io.github.trimax.venta.engine.model.instance.GizmoInstance;
import io.github.trimax.venta.engine.model.instance.implementation.GizmoInstanceImplementation;
import io.github.trimax.venta.engine.registries.implementation.MeshRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoManagerImplementation
        extends AbstractManagerImplementation<GizmoInstanceImplementation, GizmoInstance>
        implements GizmoManager {
    private final ProgramRegistryImplementation programRegistry;
    private final MeshRegistryImplementation meshRegistry;

    private GizmoInstanceImplementation origin;

    public GizmoInstanceImplementation getOrigin() {
        if (origin == null) {
            this.origin = create("Origin", GizmoType.Origin);
            this.origin.setScale(new Vector3f(100f));
        }

        return origin;
    }

    public GizmoInstanceImplementation create(final String name, final GizmoType type) {
        log.debug("Creating gizmo {}", name);

        return store(new GizmoInstanceImplementation(name,
                programRegistry.get(ProgramType.Simple.getProgramName()),
                meshRegistry.get(type.getMesh()),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f)));
    }

    @Override
    protected void destroy(final GizmoInstanceImplementation object) {
        log.debug("Destroying gizmo {} ({})", object.getID(), object.getName());
    }
}
