package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.common.effects.Particle;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.renderers.common.ParticleRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20C.glUseProgram;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterInstanceRenderer extends AbstractInstanceRenderer<EmitterInstanceImplementation, EmitterInstanceRenderer.EmitterRenderContext, SceneInstanceRenderer.SceneRenderContext> {
    private final ParticleRenderer particleRenderer;

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

        glUseProgram(emitter.getProgram().getInternalID());
        //glPolygonMode(GL_FRONT_AND_BACK, emitter.getDrawMode().getMode());

        cameraBinder.bind(emitter.getProgram(), getContext().getParent().getCamera());
        matrixBinder.bindViewProjectionMatrix(emitter.getProgram(), context.getParent().getViewProjectionMatrixBuffer());

        for (final var particle : emitter.getParticles())
            render(emitter.getProgram(), emitter.getTexture(), particle, emitter.getTransform().getMatrix());

        glUseProgram(0);
    }

    private void render(final ProgramEntityImplementation program,
                        final TextureEntityImplementation texture,
                        final Particle particle,
                        final Matrix4f modelMatrix) {
        try (var _ = particleRenderer.withContext(getContext())
                .withTexture(texture)
                .withModelMatrix(modelMatrix)
                .withProgram(program)) {
            particleRenderer.render(particle);
        }
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
