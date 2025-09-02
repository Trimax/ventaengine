package io.github.trimax.venta.engine.renderers.common;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.model.common.effects.Particle;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;
import io.github.trimax.venta.engine.renderers.instance.EmitterInstanceRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticleRenderer extends AbstractRenderer<Particle, ParticleRenderer.ParticleRenderContext, EmitterInstanceRenderer.EmitterRenderContext> {
    private final MaterialBinder materialBinder;
    private final MatrixBinder matrixBinder;

    @Override
    protected ParticleRenderContext createContext() {
        return new ParticleRenderContext();
    }

    @Override
    public void render(final Particle particle) {
        matrixBinder.bindModelMatrix(getContext().getProgram(), getContext().getModelMatrixBuffer());
        matrixBinder.bindNormalMatrix(getContext().getProgram(), getContext().getNormalMatrixBuffer());


        //TODO: Bind texture
        //materialBinder.bind(getContext().getProgram(), getContext().getMaterial());

        //TODO: Create VAO somewhere
        glBindVertexArray(particle.getVertexArrayObjectID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, particle.getFacetsBufferID());
        glDrawElements(GL_TRIANGLES, particle.getFacetsCount(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ParticleRenderContext extends AbstractRenderContext<EmitterInstanceRenderer.EmitterRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private TextureEntityImplementation texture;
        private ProgramEntityImplementation program;

        public ParticleRenderContext withModelMatrix(final Matrix4f matrix) {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();

            modelMatrix.identity()
                    .set(matrix);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
            return this;
        }

        public ParticleRenderContext withProgram(final ProgramEntityImplementation program) {
            this.program = program;
            return this;
        }

        public ParticleRenderContext withTexture(final TextureEntityImplementation texture) {
            this.texture = texture;
            return this;
        }

        @Override
        public void close() {
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();
            texture = null;
            program = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(normalMatrixBuffer);
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
