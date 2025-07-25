package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL20C.*;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.LightRenderingException;
import com.venta.engine.model.view.LightView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class LightRenderer extends AbstractRenderer<LightView, LightRenderer.LightRenderContext, ObjectRenderer.ObjectRenderContext> {

    @Override
    protected LightRenderContext createContext() {
        return new LightRenderContext();
    }

    @Override
    public void render(final LightView light) {
        if (light == null)
            return;

        final var context = getContext();
        if (context == null)
            throw new LightRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUniform1i(context.typeUniformID, 0); //TODO: Only point light supported so far
        glUniform3f(context.positionUniformID, light.getPosition().x(), light.getPosition().y(), light.getPosition().z());
        glUniform3f(context.directionUniformID, light.getDirection().x(), light.getDirection().y(), light.getDirection().z());
        glUniform3f(context.colorUniformID, light.getColor().x(), light.getColor().y(), light.getColor().z());
        glUniform1f(context.intensityUniformID, light.getColor().w());
        glUniform1i(context.castShadowsUniformID, 0); //TODO: Pass from view
        glUniform1i(context.enabledUniformID, 1);

        glUniform1f(context.attenuationConstantUniformID, light.getAttenuation().constant());
        glUniform1f(context.attenuationLinearUniformID, light.getAttenuation().linear());
        glUniform1f(context.attenuationQuadraticUniformID, light.getAttenuation().quadratic());
    }

    @Slf4j
    @Getter(AccessLevel.PACKAGE)
    static final class LightRenderContext extends AbstractRenderContext<ObjectRenderer.ObjectRenderContext> {
        private int typeUniformID;
        private int enabledUniformID;
        private int positionUniformID;
        private int directionUniformID;
        private int colorUniformID;
        private int intensityUniformID;
        private int castShadowsUniformID;

        private int attenuationConstantUniformID;
        private int attenuationLinearUniformID;
        private int attenuationQuadraticUniformID;

        public LightRenderContext withType(final int typeUniformID) {
            this.typeUniformID = typeUniformID;
            return this;
        }

        public LightRenderContext withEnabled(final int enabledUniformID) {
            this.enabledUniformID = enabledUniformID;
            return this;
        }

        public LightRenderContext withPosition(final int positionUniformID) {
            this.positionUniformID = positionUniformID;
            return this;
        }

        public LightRenderContext withDirection(final int directionUniformID) {
            this.directionUniformID = directionUniformID;
            return this;
        }

        public LightRenderContext withColor(final int colorUniformID) {
            this.colorUniformID = colorUniformID;
            return this;
        }

        public LightRenderContext withIntensity(final int intensityUniformID) {
            this.intensityUniformID = intensityUniformID;
            return this;
        }

        public LightRenderContext withCastShadows(final int castShadowsUniformID) {
            this.castShadowsUniformID = castShadowsUniformID;
            return this;
        }

        public LightRenderContext withAttenuationConstant(final int attenuationConstantUniformID) {
            this.attenuationConstantUniformID = attenuationConstantUniformID;
            return this;
        }

        public LightRenderContext withAttenuationLinear(final int attenuationLinearUniformID) {
            this.attenuationLinearUniformID = attenuationLinearUniformID;
            return this;
        }

        public LightRenderContext withAttenuationQuadratic(final int attenuationQuadraticUniformID) {
            this.attenuationQuadraticUniformID = attenuationQuadraticUniformID;
            return this;
        }

        @Override
        public void close() {
            this.typeUniformID = 0;
            this.enabledUniformID = 0;
            this.positionUniformID = 0;
            this.directionUniformID = 0;
            this.colorUniformID = 0;
            this.intensityUniformID = 0;
            this.castShadowsUniformID = 0;

            this.attenuationConstantUniformID = 0;
            this.attenuationLinearUniformID = 0;
            this.attenuationQuadraticUniformID = 0;
        }

        @Override
        public void destroy() {

        }
    }
}
