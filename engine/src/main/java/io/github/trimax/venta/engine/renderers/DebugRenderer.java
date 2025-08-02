package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.*;
import io.github.trimax.venta.engine.model.view.CameraView;
import io.github.trimax.venta.engine.model.view.LightView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import one.util.streamex.StreamEx;
import org.joml.Vector3f;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugRenderer extends AbstractRenderer<SceneView, SceneRenderer.SceneRenderContext, SceneRenderer.SceneRenderContext> {
    private static final Vector3f VECTOR_INFINITY = new Vector3f(100000);
    private static final Vector3f VECTOR_ZERO = new Vector3f(0);
    private static final Vector3f VECTOR_ONE = new Vector3f(1);

    private final CameraManagerImplementation.CameraAccessor cameraAccessor;
    private final ObjectManagerImplementation.ObjectAccessor objectAccessor;
    private final LightManagerImplementation.LightAccessor lightAccessor;
    private final SceneManagerImplementation.SceneAccessor sceneAccessor;
    private final GizmoRenderer gizmoRenderer;
    private final GizmoManagerImplementation gizmoManager;

    @Override
    protected SceneRenderer.SceneRenderContext createContext() {
        return new SceneRenderer.SceneRenderContext();
    }

    @Override
    public void render(final SceneView scene) {
        render(sceneAccessor.get(scene));
    }

    private void render(final SceneManagerImplementation.SceneEntity scene) {
        renderOrigin();

        StreamEx.of(scene.getLights()).forEach(this::render);
        StreamEx.of(scene.getObjects()).forEach(this::render);

        StreamEx.of(cameraAccessor.iterator()).forEach(this::render);
    }

    private void render(final ObjectView object) {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())) {
            gizmoRenderer.render(objectAccessor.get(object).getGizmo());
        }
    }

    private void render(final LightView light) {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(light.getPosition(), VECTOR_ZERO, VECTOR_ONE)) {
            gizmoRenderer.render(lightAccessor.get(light).getGizmo());
        }
    }

    private void render(final CameraView camera) {
        if (camera == getContext().getCamera())
            return;

        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(cameraAccessor.get(camera))) {
            gizmoRenderer.render(cameraAccessor.get(camera).getGizmo());
        }
    }

    private void renderOrigin() {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(VECTOR_ZERO, VECTOR_ZERO, VECTOR_INFINITY)) {
            gizmoRenderer.render(gizmoManager.getOrigin());
        }
    }
}
