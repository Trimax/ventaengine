package io.github.trimax.venta.engine.renderers.common;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.ParticleBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.effects.Particle;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;
import io.github.trimax.venta.engine.renderers.instance.EmitterInstanceRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticleRenderer extends AbstractRenderer<Particle, ParticleRenderer.ParticleRenderContext, EmitterInstanceRenderer.EmitterRenderContext> {
    private final ParticleBinder particleBinder;
    private final TextureBinder textureBinder;
    private final MatrixBinder matrixBinder;

    @Override
    protected ParticleRenderContext createContext() {
        return new ParticleRenderContext();
    }

    @Override
    public void render(final Particle particle) {
        final var emitter = getContext().getEmitter();
        matrixBinder.bindViewProjectionMatrix(emitter.getProgram(), getContext().getParent().getParent().getViewProjectionMatrixBuffer());
        matrixBinder.bindModelMatrix(emitter.getProgram(), getContext().getModelMatrixBuffer());

        particleBinder.bind(emitter.getProgram(), particle);
        textureBinder.bind(TextureType.Diffuse, emitter.getProgram(), emitter.getTexture());

        glBindVertexArray(emitter.getParticleVertexArrayObjectID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, emitter.getParticleFacesBufferID());
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ParticleRenderContext extends AbstractRenderContext<EmitterInstanceRenderer.EmitterRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final Matrix4f modelMatrix = new Matrix4f();

        private EmitterInstanceImplementation emitter;

        public ParticleRenderContext withViewMatrix(final Matrix4fc viewMatrix, final Particle particle) {
            modelMatrixBuffer.clear();

            modelMatrix.identity()
                    .translate(particle.getPosition())
                    .scale(particle.getSize());

            applyBillboard(modelMatrix, viewMatrix);
            modelMatrix.get(modelMatrixBuffer);

            return this;
        }

        public ParticleRenderContext withEmitter(final EmitterInstanceImplementation emitter) {
            this.emitter = emitter;
            return this;
        }

        @Override
        public void close() {
            modelMatrixBuffer.clear();
            emitter = null;
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(modelMatrixBuffer);
        }

        private void applyBillboard(final Matrix4f model, final Matrix4fc viewMatrix) {
            final var billboardRotation = new Matrix4f(viewMatrix);
            billboardRotation.m30(0);
            billboardRotation.m31(0);
            billboardRotation.m32(0);

            billboardRotation.invert();
            model.mul(billboardRotation);
        }
    }
}
