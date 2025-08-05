package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CameraManagerImplementation
        extends AbstractManagerImplementation<CameraInstanceImplementation, CameraInstance>
        implements CameraManager {
    private final GizmoManagerImplementation gizmoManager;

    @Getter(onMethod_ = @__(@Override))
    private CameraInstanceImplementation current;

    @Override
    public CameraInstanceImplementation create(@NonNull final String name) {
        log.info("Creating camera {}", name);

        return store(new CameraInstanceImplementation(name, new Vector3f(0, 0, 3), new Vector3f(0, 0, 0),
                gizmoManager.create("camera", GizmoType.Camera)));
    }

    @Override
    public void setCurrent(@NonNull final CameraInstance camera) {
        if (camera instanceof CameraInstanceImplementation entity)
            this.current = entity;
    }

    @Override
    protected void destroy(final CameraInstanceImplementation camera) {
        log.info("Destroying camera {} ({})", camera.getID(), camera.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
