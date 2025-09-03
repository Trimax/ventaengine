package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.renderers.common.ParticleRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.lwjgl.opengl.GL11C.*;
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

        glPolygonMode(GL_FRONT_AND_BACK, DrawMode.Polygon.getMode());
        glUseProgram(emitter.getProgram().getInternalID());

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        cameraBinder.bind(emitter.getProgram(), getContext().getParent().getCamera());
        matrixBinder.bindViewProjectionMatrix(emitter.getProgram(), context.getParent().getViewProjectionMatrixBuffer());

        for (final var particle : emitter.getParticles())
            try (var _ = particleRenderer.withContext(getContext())
                    .withModelMatrix(emitter.getTransform().getMatrix())
                    .withEmitter(emitter)) {
                particleRenderer.render(particle);
            }

        glDepthMask(true);
        glDisable(GL_BLEND);

        glUseProgram(0);
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
