package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.GizmoManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import io.github.trimax.venta.engine.model.view.GizmoView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GizmoRenderer extends AbstractRenderer<GizmoView, GizmoRenderer.GizmoRenderContext, SceneRenderer.SceneRenderContext> {
    private final ProgramManagerImplementation.ProgramAccessor programAccessor;
    private final GizmoManagerImplementation.GizmoAccessor gizmoAccessor;
    private final ProgramManagerImplementation programManager;
    private final MeshRenderer meshRenderer;
    private final MatrixBinder matrixBinder;

    @Override
    protected GizmoRenderContext createContext() {
        return new GizmoRenderContext();
    }

    @Override
    void render(final GizmoView gizmo) {
        render(gizmoAccessor.get(gizmo), programAccessor.get(programManager.load(ProgramType.Simple.name())));
    }

    private void render(final GizmoManagerImplementation.GizmoEntity gizmo, final ProgramManagerImplementation.ProgramEntity program) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(program.getInternalID());
        glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Edge.getMode());

        matrixBinder.bind(program, context.getParent().getViewProjectionMatrixBuffer(), context.getModelMatrixBuffer(), context.getNormalMatrixBuffer());

        try (var _ = meshRenderer.withContext(null)
                .withProgram(program)) {
            meshRenderer.render(gizmo.getMesh());
        }

        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class GizmoRenderContext extends AbstractRenderContext<SceneRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        public GizmoRenderContext withModelMatrix(final Vector3f position, final Vector3f rotation, final Vector3f scale) {
            modelMatrix.identity()
                    .translate(position)
                    .rotateX(rotation.x)
                    .rotateY(rotation.y)
                    .rotateZ(rotation.z)
                    .scale(scale)
                    .get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public GizmoRenderContext withModelMatrix(final CameraManagerImplementation.CameraEntity camera) {
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
