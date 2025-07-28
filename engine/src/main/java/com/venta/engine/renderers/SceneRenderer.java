package com.venta.engine.renderers;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.core.VentaContext;
import com.venta.engine.helpers.DebugHelper;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneRenderer extends AbstractRenderer<SceneView, SceneRenderer.SceneRenderContext, SceneRenderer.SceneRenderContext> {
    private final SceneManager.SceneAccessor sceneAccessor;
    private final ObjectRenderer objectRenderer;
    private final VentaContext ventaContext;
    private final DebugHelper debugHelper;

    @Override
    protected SceneRenderContext createContext() {
        return new SceneRenderContext();
    }

    @Override
    @SneakyThrows
    public void render(final SceneView scene) {
        if (scene == null)
            return;

        for (final ObjectView object : scene.getObjects())
            try (final var _ = objectRenderer.withContext(getContext())
                    .withModelMatrix(object.getPosition(), object.getRotation(), object.getScale())
                    .withScene(scene)) {
                objectRenderer.render(object);
            }

        if (ventaContext.getRenderConfiguration().isDebugEnabled())
            renderDebugVisuals(scene, sceneAccessor.get(scene).getDebugVisuals());
    }

    private void renderDebugVisuals(final SceneView scene, final List<DebugHelper.DebugVisual> debugVisuals) {
        for (final var visual : debugVisuals) {
            if (!visual.hasRenderableObject())
                visual.setRenderableObject(debugHelper.create(visual.getReferencedObject()));

            try (final var _ = objectRenderer.withContext(getContext())
                    .withModelMatrix(visual.getPosition(), visual.getRotation(), visual.getScale())
                    .withScene(scene)) {
                objectRenderer.render(visual.getRenderableObject());
            }
        }
    }

    @Getter(AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext<SceneRenderContext> {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();
        private CameraView camera;

        public SceneRenderContext with(final WindowManager.WindowEntity window, final CameraManager.CameraEntity camera) {
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
