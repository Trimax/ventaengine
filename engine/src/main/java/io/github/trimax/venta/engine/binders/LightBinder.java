package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderLightUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entities.ProgramEntity;
import io.github.trimax.venta.engine.model.view.LightView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightBinder extends AbstractBinder {
    public void bind(final ProgramEntity program, final Vector4f ambientLight) {
        bind(program.getUniformID(ShaderUniform.AmbientLight), ambientLight);
    }

    public void bind(final ProgramEntity program, final List<? extends LightView> lights) {
        bind(program.getUniformID(ShaderUniform.LightCount), lights.size());
        for (int lightID = 0; lightID < lights.size(); lightID++)
            bind(program, lights.get(lightID), lightID);
    }

    private void bind(final ProgramEntity program, final LightView light, final int lightIndex) {
        bind(program.getUniformID(ShaderLightUniform.Type.getUniformName(lightIndex)), 0); //TODO: Only point light supported so far
        bind(program.getUniformID(ShaderLightUniform.Enabled.getUniformName(lightIndex)), 1);
        bind(program.getUniformID(ShaderLightUniform.CastShadows.getUniformName(lightIndex)), 0); //TODO: Pass from view

        /* Position and direction */
        bind(program.getUniformID(ShaderLightUniform.Position.getUniformName(lightIndex)), light.getPosition());
        bind(program.getUniformID(ShaderLightUniform.Direction.getUniformName(lightIndex)), light.getDirection());

        /* Color and intensity */
        bind(program.getUniformID(ShaderLightUniform.Color.getUniformName(lightIndex)), light.getColor());
        bind(program.getUniformID(ShaderLightUniform.Intensity.getUniformName(lightIndex)), light.getIntensity());

        /* Attenuation */
        bind(program.getUniformID(ShaderLightUniform.AttenuationLinear.getUniformName(lightIndex)), light.getAttenuation().linear());
        bind(program.getUniformID(ShaderLightUniform.AttenuationConstant.getUniformName(lightIndex)), light.getAttenuation().constant());
        bind(program.getUniformID(ShaderLightUniform.AttenuationQuadratic.getUniformName(lightIndex)), light.getAttenuation().quadratic());
    }
}
