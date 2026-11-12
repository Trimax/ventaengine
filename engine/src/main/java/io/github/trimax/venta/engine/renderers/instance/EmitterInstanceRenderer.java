package io.github.trimax.venta.engine.renderers.instance;

import static org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11C.glPolygonMode;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.EmitterBinder;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.TextureBinder;
import io.github.trimax.venta.engine.definitions.DefinitionsGeometry;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.utils.BufferUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterInstanceRenderer extends AbstractInstanceRenderer<EmitterInstanceImplementation, EmitterInstanceRenderer.EmitterRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final Matrix4f matrixTemporaryModel = new Matrix4f();
    private final Matrix4f matrixTemporaryView = new Matrix4f();

    private final EmitterBinder emitterBinder;
    private final TextureBinder textureBinder;
    private final MatrixBinder matrixBinder;
    private final CameraBinder cameraBinder;
    private final LightBinder lightBinder;
    private final FogBinder fogBinder;

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

        glUseProgram(emitter.getProgram().getInternalID());

        final var scene = context.getScene();
        if (context.getParent().markLightsBound(emitter.getProgram())) {
            lightBinder.bind(emitter.getProgram(), scene.getDirectionalLight());
            lightBinder.bind(emitter.getProgram(), scene.getAmbientLight());
            lightBinder.bind(emitter.getProgram(), scene.getLights());
        }

        emitterBinder.bind(emitter.getProgram(), emitter);
        fogBinder.bind(emitter.getProgram(), scene.getFog());
        cameraBinder.bind(emitter.getProgram(), getContext().getParent().getCamera());
        matrixBinder.bindViewProjectionMatrix(emitter.getProgram(), context.getParent().getViewProjectionMatrixBuffer());
        textureBinder.bind(TextureType.Diffuse, emitter.getProgram(), emitter.getTexture());

        prepareParticles(emitter);

        glBindBuffer(GL_ARRAY_BUFFER, emitter.getParticleInstanceBufferID());
        glBufferSubData(GL_ARRAY_BUFFER, 0, emitter.getBufferMatrixModel());

        glBindBuffer(GL_ARRAY_BUFFER, emitter.getParticleColorBufferID());
        glBufferSubData(GL_ARRAY_BUFFER, 0, emitter.getBufferColor());

        glBindVertexArray(emitter.getParticleVertexArrayObjectID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, emitter.getParticleFacesBufferID());
        glDrawElementsInstanced(GL_TRIANGLES, DefinitionsGeometry.Particle.FACETS.length, GL_UNSIGNED_INT, 0, emitter.getParticles().size());
        glBindVertexArray(0);

        glUseProgram(0);
    }

    private void prepareParticles(final EmitterInstanceImplementation emitter) {
        final var bufferMatrixModel = emitter.getBufferMatrixModel();
        final var bufferColor = emitter.getBufferColor();

        bufferMatrixModel.clear();
        bufferColor.clear();

        for (final var particle : emitter.getParticles()) {
            BufferUtil.write(particle.getColor(), bufferColor);

            matrixTemporaryModel.identity().translate(particle.getPosition());
            applyBillboard(matrixTemporaryModel, getContext().getParent().getViewMatrix());
            matrixTemporaryModel.rotateZ(particle.getRotation());
            matrixTemporaryModel.scale(particle.getSize());

            BufferUtil.write(matrixTemporaryModel, bufferMatrixModel);
        }

        bufferMatrixModel.flip();
        bufferColor.flip();
    }

    private void applyBillboard(final Matrix4f model, final Matrix4fc viewMatrix) {
        matrixTemporaryView.set(viewMatrix);
        matrixTemporaryView.m30(0);
        matrixTemporaryView.m31(0);
        matrixTemporaryView.m32(0);

        matrixTemporaryView.invert();
        model.mul(matrixTemporaryView);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class EmitterRenderContext extends AbstractRenderContext<SceneInstanceRenderer.SceneRenderContext> {
        private SceneInstanceImplementation scene;

        public EmitterRenderContext withScene(final SceneInstanceImplementation scene) {
            this.scene = scene;
            return this;
        }

        @Override
        public void close() {
            scene = null;
        }

        @Override
        public void destroy() {
        }
    }
}
