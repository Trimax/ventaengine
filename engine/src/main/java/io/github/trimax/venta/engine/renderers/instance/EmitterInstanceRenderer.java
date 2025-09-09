package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.definitions.GeometryDefinitions;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.renderers.common.ParticleRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterInstanceRenderer extends AbstractInstanceRenderer<EmitterInstanceImplementation, EmitterInstanceRenderer.EmitterRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final ParticleRenderer particleRenderer;

    private final TextureBinder textureBinder;
    private final MatrixBinder matrixBinder;
    private final CameraBinder cameraBinder;

    @Override
    protected EmitterRenderContext createContext() {
        return new EmitterRenderContext();
    }

    @Override
    public void render(final EmitterInstanceImplementation emitter) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Polygon.getMode());

        FloatBuffer modelMatrixBuffer = emitter.getModelMatrixBuffer();
        int maxParticles = modelMatrixBuffer.capacity() / 16; // 16 float на матрицу
        System.out.println("Buffer can hold up to " + maxParticles + " particles");
        System.out.println("Current particle count: " + emitter.getParticles().size());

        glUseProgram(emitter.getProgram().getInternalID());
        cameraBinder.bind(emitter.getProgram(), getContext().getParent().getCamera());
        matrixBinder.bindViewProjectionMatrix(emitter.getProgram(), context.getParent().getViewProjectionMatrixBuffer());
        textureBinder.bind(TextureType.Diffuse, emitter.getProgram(), emitter.getTexture());


        // обновляем буфер матриц
        modelMatrixBuffer.clear();
        for (final var particle : emitter.getParticles()) {
            Matrix4f m = new Matrix4f().translate(particle.getPosition()).scale(particle.getSize());
            applyBillboard(m, getContext().getParent().getViewMatrix());

            float[] tmp = new float[16];
            m.get(tmp); // записываем матрицу в массив

            for (float f : tmp) modelMatrixBuffer.put(f); // вручную в FloatBuffer

//
//            final var matrixModel = new Matrix4f().translate(particle.getPosition()).scale(particle.getSize());
//            applyBillboard(matrixModel, getContext().getParent().getViewMatrix());
//            matrixModel.get(modelMatrixBuffer);
        }

        modelMatrixBuffer.flip(); // готовим к чтению

        int toPrint = Math.min(16, modelMatrixBuffer.remaining()); // не выйдем за предел
        for (int i = 0; i < toPrint; i++) {
            System.out.print(modelMatrixBuffer.get() + " "); // get() сдвигает позицию
        }
        System.out.println();

        modelMatrixBuffer.rewind(); // обратно в начало для glBufferSubData


        glBindBuffer(GL_ARRAY_BUFFER, emitter.getParticleInstanceBufferID());





        glBufferSubData(GL_ARRAY_BUFFER, 0, modelMatrixBuffer);

        // draw call
        glBindVertexArray(emitter.getParticleVertexArrayObjectID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, emitter.getParticleFacesBufferID());
        glDrawElementsInstanced(GL_TRIANGLES, GeometryDefinitions.PARTICLE_INDICES.length, GL_UNSIGNED_INT, 0, emitter.getParticles().size());
        glBindVertexArray(0);

        glUseProgram(0);
    }

    private void applyBillboard(final Matrix4f model, final Matrix4fc viewMatrix) {
        final var billboardRotation = new Matrix4f(viewMatrix);
        billboardRotation.m30(0);
        billboardRotation.m31(0);
        billboardRotation.m32(0);

        billboardRotation.invert();
        model.mul(billboardRotation);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class EmitterRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        @Override
        public void close() {
        }

        @Override
        public void destroy() {
        }
    }
}
