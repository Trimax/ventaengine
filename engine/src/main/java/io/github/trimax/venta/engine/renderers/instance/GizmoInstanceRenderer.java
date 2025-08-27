package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.GizmoInstanceImplementation;
import io.github.trimax.venta.engine.renderers.common.DebugRenderer;
import io.github.trimax.venta.engine.renderers.entity.MeshEntityRenderer;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoInstanceRenderer extends AbstractInstanceRenderer<GizmoInstanceImplementation, GizmoInstanceRenderer.GizmoRenderContext, DebugRenderer.DebugRenderContext> {
    private final MeshEntityRenderer meshRenderer;
    private final MatrixBinder matrixBinder;

    @Override
    protected GizmoRenderContext createContext() {
        return new GizmoRenderContext();
    }

    @Override
    public void render(final GizmoInstanceImplementation gizmo) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(gizmo.getProgram().getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Edge.getMode());

        matrixBinder.bindViewProjectionMatrix(gizmo.getProgram(), context.getParent().getViewProjectionMatrixBuffer());

        try (var _ = meshRenderer.withContext(null)
                .withModelMatrix(context.getModelMatrix())
                .withProgram(gizmo.getProgram())) {
            meshRenderer.render(gizmo.getMesh());
        }

        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class GizmoRenderContext extends AbstractRenderContext<DebugRenderer.DebugRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        public GizmoRenderContext withModelMatrix(@NonNull final Vector3fc position, @NonNull final Vector3fc rotation, @NonNull final Vector3fc scale) {
            modelMatrix.identity()
                    .translate(position)
                    .rotateX(rotation.x())
                    .rotateY(rotation.y())
                    .rotateZ(rotation.z())
                    .scale(scale)
                    .get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public GizmoRenderContext withModelMatrix(final CameraInstanceImplementation camera) {
            modelMatrix.set(camera.getViewMatrix()).invert();

            modelMatrix.get(modelMatrixBuffer);
            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);

            return this;
        }

        @Override
        public void close() {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(normalMatrixBuffer);
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
