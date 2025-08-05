package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.view.CameraView;
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
        extends AbstractManagerImplementation<CameraInstance, CameraView>
        implements CameraManager {
    private final GizmoManagerImplementation gizmoManager;

    @Getter(onMethod_ = @__(@Override))
    private CameraInstance current;

    @Override
    public CameraInstance create(@NonNull final String name) {
        log.info("Creating camera {}", name);

        return store(new CameraInstance(name, new Vector3f(0, 0, 3), new Vector3f(0, 0, 0),
                gizmoManager.create("camera", GizmoType.Camera)));
    }

    @Override
    public void setCurrent(@NonNull final CameraView camera) {
        if (camera instanceof CameraInstance entity)
            this.current = entity;
    }

    @Override
    protected void destroy(final CameraInstance camera) {
        log.info("Destroying camera {} ({})", camera.getID(), camera.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
