package com.venta.engine.renderers;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.managers.AbstractManager;
import com.venta.engine.model.view.CameraView;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRenderer<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>> {
    @Getter(AccessLevel.PROTECTED)
    private RenderContext context;

    abstract void render(final V view);

    @AllArgsConstructor
    public abstract static class AbstractView<E extends AbstractManager.AbstractEntity> {
        @Getter
        @NonNull
        private final String id;

        @NonNull
        protected final E entity;
    }

    @Getter(AccessLevel.PACKAGE)
    static final class RenderContext implements AutoCloseable {
        private final FloatBuffer projectionMatrixBuffer;
        private final FloatBuffer viewMatrixBuffer;
        private final List<LightView> lights;
        private final Vector4f ambientLight;

        public RenderContext(final CameraView camera, final WindowView window, final List<LightView> lights, final Vector4f ambientLight) {
            this.viewMatrixBuffer = MemoryUtil.memAllocFloat(16);
            camera.entity.getViewMatrix().get(viewMatrixBuffer);

            this.projectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
            window.entity.getProjectionMatrix().get(projectionMatrixBuffer);

            this.ambientLight = ambientLight;
            this.lights = lights;
        }

        @Override
        public void close() {
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }

    final AutoCloseable withContext(@NonNull final RenderContext context) {
        this.context = context;
        return () -> {
            this.context = null;
            context.close();
        };
    }
}
