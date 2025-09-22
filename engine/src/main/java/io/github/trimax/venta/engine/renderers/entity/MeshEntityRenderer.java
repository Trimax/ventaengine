package io.github.trimax.venta.engine.renderers.entity;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.renderers.instance.ObjectInstanceRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshEntityRenderer extends AbstractEntityRenderer<MeshEntityImplementation, MeshEntityRenderer.MeshRenderContext, ObjectInstanceRenderer.ObjectRenderContext> {
    private final GeometryHelper geometryHelper;
    private final MaterialBinder materialBinder;
    private final MatrixBinder matrixBinder;

    @Override
    protected MeshRenderContext createContext() {
        return new MeshRenderContext();
    }

    @Override
    public void render(final MeshEntityImplementation object) {
        matrixBinder.bindModelMatrix(getContext().getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindNormalMatrix(getContext().getProgram(), getContext().getNormalMatrixBuffer());

        materialBinder.bind(getContext().getProgram(), getContext().getMaterial());

        geometryHelper.render(object.getGeometry());
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class MeshRenderContext extends AbstractRenderContext<ObjectInstanceRenderer.ObjectRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private MaterialEntityImplementation material;
        private ProgramEntityImplementation program;

        public MeshRenderContext withModelMatrix(final Matrix4f matrix) {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();

            modelMatrix.identity()
                    .set(matrix);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public MeshRenderContext withProgram(final ProgramEntityImplementation program) {
            this.program = program;
            return this;
        }

        public MeshRenderContext withMaterial(final MaterialEntityImplementation material) {
            this.material = material;
            return this;
        }

        @Override
        public void close() {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();
            material = null;
            program = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(normalMatrixBuffer);
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
