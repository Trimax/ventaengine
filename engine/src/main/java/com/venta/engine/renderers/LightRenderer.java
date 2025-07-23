package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL20C.*;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.LightRenderingException;
import com.venta.engine.managers.LightManager;
import com.venta.engine.model.core.Counter;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ProgramView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class LightRenderer extends AbstractRenderer<LightManager.LightEntity, LightView, LightRenderer.LightRenderContext> {
    @Override
    public void render(final LightView light) {
        if (light == null)
            return;

        final var context = getContext();
        if (context == null)
            throw new LightRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        final var prefix = "lights[" + context.getCounter().getAndIncrement() + "]";
        glUniform1i(context.getProgram().entity.getUniformID(prefix + ".type"), 0); //TODO: Only point light supported so far
        glUniform3f(context.getProgram().entity.getUniformID(prefix + ".position"),
                light.getPosition().x(), light.getPosition().y(), light.getPosition().z());
        glUniform3f(context.getProgram().entity.getUniformID(prefix + ".direction"),
                light.getDirection().x(), light.getDirection().y(), light.getDirection().z());
        glUniform3f(context.getProgram().entity.getUniformID(prefix + ".color"),
                light.getColor().x(), light.getColor().y(), light.getColor().z());
        glUniform1f(context.getProgram().entity.getUniformID(prefix + ".intensity"), light.getColor().w());
        glUniform1i(context.getProgram().entity.getUniformID(prefix + ".castShadows"), 0); //TODO: Pass from view
        glUniform1i(context.getProgram().entity.getUniformID(prefix + ".enabled"), 1);

        setAttenuation(context, prefix, light.getAttenuation());
    }

    private void setAttenuation(final LightRenderContext context, final String prefix, final LightView.Attenuation attenuation) {
        glUniform1f(context.getProgram().entity.getUniformID(prefix + ".attenuation.constant"), attenuation.constant());
        glUniform1f(context.getProgram().entity.getUniformID(prefix + ".attenuation.linear"), attenuation.linear());
        glUniform1f(context.getProgram().entity.getUniformID(prefix + ".attenuation.quadratic"), attenuation.quadratic());
    }

    @SuperBuilder
    @Getter(AccessLevel.PACKAGE)
    static final class LightRenderContext extends AbstractRenderContext {
        private final Counter counter = new Counter();

        @NonNull
        private final ProgramView program;

        @Override
        public void close() {

        }
    }
}
