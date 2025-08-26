package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.renderers.entity.CubemapEntityRenderer;
import lombok.*;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneInstanceRenderer
        extends AbstractInstanceRenderer<SceneInstanceImplementation, SceneInstanceRenderer.SceneRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final ObjectManagerImplementation objectManager;
    private final ObjectInstanceRenderer objectRenderer;
    private final CubemapEntityRenderer cubemapRenderer;

    @Override
    public SceneRenderContext createContext() {
        return new SceneRenderContext();
    }

    @Override
    @SneakyThrows
    public void render(final SceneInstanceImplementation scene) {
        if (scene == null)
            return;

        final var cubemap = scene.getCubemap();
        if (cubemap != null)
            try (final var _ = cubemapRenderer.withContext(getContext())
                    .withScene(scene)) {
                cubemapRenderer.render(cubemap);
            }

        for (final ObjectInstance object : scene.getObjects())
            try (final var _ = objectRenderer.withContext(getContext())
                    .withScene(scene)) {
                objectRenderer.render(objectManager.getInstance(object.getID()));
            }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class SceneRenderContext extends AbstractRenderContext<SceneRenderContext> {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer projectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer viewMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f viewProjectionMatrix = new Matrix4f();
        private final Matrix4f projectionMatrix = new Matrix4f();
        private final Matrix4f viewMatrix = new Matrix4f();
        private CameraInstanceImplementation camera;

        public SceneRenderContext with(final WindowState window, final CameraInstanceImplementation camera) {
            window.getProjectionMatrix().mul(camera.getViewMatrix(), viewProjectionMatrix);
            projectionMatrix.set(window.getProjectionMatrix());
            viewMatrix.set(camera.getViewMatrix());

            viewProjectionMatrix.get(viewProjectionMatrixBuffer);
            projectionMatrix.get(projectionMatrixBuffer);
            viewMatrix.get(viewMatrixBuffer);
            this.camera = camera;

            return this;
        }

        @Override
        public void close() {
            viewProjectionMatrixBuffer.clear();
            projectionMatrixBuffer.clear();
            viewMatrixBuffer.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }
}
