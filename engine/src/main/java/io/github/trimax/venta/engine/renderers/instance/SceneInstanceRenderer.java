package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.managers.implementation.EmitterManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WaterSurfaceManagerImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.renderers.entity.CubemapEntityRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneInstanceRenderer
        extends AbstractInstanceRenderer<SceneInstanceImplementation, SceneInstanceRenderer.SceneRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final WaterSurfaceManagerImplementation gridMeshManager;
    private final EmitterManagerImplementation emitterManager;
    private final ObjectManagerImplementation objectManager;
    private final BillboardInstanceRenderer billboardRenderer;
    private final WaterSurfaceInstanceRenderer gridMeshRenderer;
    private final EmitterInstanceRenderer emitterRenderer;
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

        final var cubemap = scene.getSkybox();
        if (cubemap != null)
            try (final var _ = cubemapRenderer.withContext(getContext())
                    .withScene(scene)) {
                cubemapRenderer.render(cubemap);
            }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (final var gridMesh : scene.getWaterSurfaces())
            try (final var _ = gridMeshRenderer.withContext(getContext())
                    .withModelMatrix(gridMesh.getTransform().getMatrix())
                    .withScene(scene)) {
                gridMeshRenderer.render(gridMeshManager.getInstance(gridMesh.getID()));
            }

        for (final var object : scene.getObjects())
            try (final var _ = objectRenderer.withContext(getContext())
                    .withScene(scene)) {
                objectRenderer.render(objectManager.getInstance(object.getID()));
            }

        for (final var billboard : scene.getBillboards())
            try (final var _ = billboardRenderer.withContext(getContext())
                    .withModelMatrix(billboard.getTransform().getMatrix())) {
                billboardRenderer.render(billboard);
            }

        glDepthMask(false);
        for (final var emitter : scene.getEmitters())
            try (final var _ = emitterRenderer.withContext(getContext())) {
                emitterRenderer.render(emitterManager.getInstance(emitter.getID()));
            }
        glDepthMask(true);
        glDisable(GL_BLEND);
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
        private Engine.VentaTime time;

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

        public SceneRenderContext withTime(final Engine.VentaTime time) {
            this.time = time;
            return this;
        }

        @Override
        public void close() {
            viewProjectionMatrixBuffer.clear();
            projectionMatrixBuffer.clear();
            viewMatrixBuffer.clear();
            time = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }
}
