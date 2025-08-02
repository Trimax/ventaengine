package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import one.util.streamex.StreamEx;
import org.joml.Vector3f;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugRenderer extends AbstractRenderer<SceneManagerImplementation.SceneEntity, SceneRenderer.SceneRenderContext, SceneRenderer.SceneRenderContext> {
    private static final Vector3f VECTOR_INFINITY = new Vector3f(100000);
    private static final Vector3f VECTOR_ZERO = new Vector3f(0);
    private static final Vector3f VECTOR_ONE = new Vector3f(1);

    private final CameraManagerImplementation cameraManager;
    private final GizmoManagerImplementation gizmoManager;
    private final GizmoRenderer gizmoRenderer;

    @Override
    protected SceneRenderer.SceneRenderContext createContext() {
        return new SceneRenderer.SceneRenderContext();
    }

    @Override
    public void render(final SceneManagerImplementation.SceneEntity scene) {
        renderOrigin();

        StreamEx.of(scene.getLights()).forEach(this::render);
        StreamEx.of(scene.getObjects()).forEach(this::render);

        StreamEx.of(cameraManager.entityIterator()).forEach(this::render);
    }

    private void render(final ObjectManagerImplementation.ObjectEntity object) {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())) {
            gizmoRenderer.render(object.getGizmo());
        }
    }

    private void render(final LightManagerImplementation.LightEntity light) {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(light.getPosition(), VECTOR_ZERO, VECTOR_ONE)) {
            gizmoRenderer.render(light.getGizmo());
        }
    }

    private void render(final CameraManagerImplementation.CameraEntity camera) {
        if (camera == getContext().getCamera())
            return;

        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(camera)) {
            gizmoRenderer.render(camera.getGizmo());
        }
    }

    private void renderOrigin() {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(VECTOR_ZERO, VECTOR_ZERO, VECTOR_INFINITY)) {
            gizmoRenderer.render(gizmoManager.getOrigin());
        }
    }
}
