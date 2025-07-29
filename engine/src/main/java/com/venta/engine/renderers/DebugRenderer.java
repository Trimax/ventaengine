package com.venta.engine.renderers;

import org.joml.Vector3f;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.GizmoManager;
import com.venta.engine.managers.LightManager;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import one.util.streamex.StreamEx;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugRenderer extends AbstractRenderer<SceneView, SceneRenderer.SceneRenderContext, SceneRenderer.SceneRenderContext> {
    private static final Vector3f VECTOR_INFINITY = new Vector3f(100000);
    private static final Vector3f VECTOR_ZERO = new Vector3f(0);
    private static final Vector3f VECTOR_ONE = new Vector3f(1);

    private final CameraManager.CameraAccessor cameraAccessor;
    private final ObjectManager.ObjectAccessor objectAccessor;
    private final LightManager.LightAccessor lightAccessor;
    private final SceneManager.SceneAccessor sceneAccessor;
    private final GizmoRenderer gizmoRenderer;
    private final GizmoManager gizmoManager;

    @Override
    protected SceneRenderer.SceneRenderContext createContext() {
        return new SceneRenderer.SceneRenderContext();
    }

    @Override
    public void render(final SceneView scene) {
        render(sceneAccessor.get(scene));
    }

    private void render(final SceneManager.SceneEntity scene) {
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

    }

    private void renderOrigin() {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(VECTOR_ZERO, VECTOR_ZERO, VECTOR_INFINITY)) {
            gizmoRenderer.render(gizmoManager.getOrigin());
        }
    }
}
