package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.GizmoManagerImplementation;
import io.github.trimax.venta.engine.model.entity.CameraEntity;
import io.github.trimax.venta.engine.model.entity.LightEntity;
import io.github.trimax.venta.engine.model.entity.ObjectEntity;
import io.github.trimax.venta.engine.model.entity.SceneEntity;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.model.view.CameraView;
import io.github.trimax.venta.engine.renderers.entity.GizmoRenderer;
import io.github.trimax.venta.engine.renderers.entity.SceneRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.util.streamex.StreamEx;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugRenderer extends AbstractRenderer<SceneEntity, DebugRenderer.DebugRenderContext, SceneRenderer.SceneRenderContext> {
    private static final Vector3f VECTOR_INFINITY = new Vector3f(100000);
    private static final Vector3f VECTOR_ZERO = new Vector3f(0);
    private static final Vector3f VECTOR_ONE = new Vector3f(1);

    private final CameraManagerImplementation cameraManager;
    private final GizmoManagerImplementation gizmoManager;
    private final GizmoRenderer gizmoRenderer;

    @Override
    protected DebugRenderContext createContext() {
        return new DebugRenderContext();
    }

    @Override
    public void render(final SceneEntity scene) {
        renderOrigin();

        StreamEx.of(scene.getLights()).forEach(this::render);
        StreamEx.of(scene.getObjects()).forEach(this::render);

        StreamEx.of(cameraManager.entityIterator()).forEach(this::render);
    }

    private void render(final ObjectEntity object) {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())) {
            gizmoRenderer.render(object.getGizmo());
        }
    }

    private void render(final LightEntity light) {
        try (final var _ = gizmoRenderer.withContext(getContext())
                .withModelMatrix(light.getPosition(), VECTOR_ZERO, VECTOR_ONE)) {
            gizmoRenderer.render(light.getGizmo());
        }
    }

    private void render(final CameraEntity camera) {
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

    @Getter
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class DebugRenderContext extends AbstractRenderContext<SceneRenderer.SceneRenderContext> {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();
        private CameraView camera;

        public DebugRenderContext with(final WindowState window, final CameraEntity camera) {
            window.getProjectionMatrix().mul(camera.getViewMatrix(), viewProjectionMatrix);
            viewProjectionMatrix.get(viewProjectionMatrixBuffer);
            this.camera = camera;

            return this;
        }

        @Override
        public void close() {
            viewProjectionMatrixBuffer.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
        }
    }
}
