package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderLightUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final Vector4f ambientLight) {
        bind(program.getUniformID(ShaderUniform.AmbientLight), ambientLight);
    }

    public void bind(final ProgramEntityImplementation program, final List<? extends LightInstance> lights) {
        bind(program.getUniformID(ShaderUniform.LightCount), lights.size());
        for (int lightID = 0; lightID < lights.size(); lightID++)
            bind(program, lights.get(lightID), lightID);
    }

    private void bind(final ProgramEntityImplementation program, final LightInstance light, final int lightIndex) {
        if (light instanceof LightInstanceImplementation instance)
            bind(program, instance, lightIndex);
    }

    private void bind(final ProgramEntityImplementation program, final LightInstanceImplementation light, final int lightIndex) {
        bind(program.getUniformID(ShaderLightUniform.Type.getUniformName(lightIndex)), light.getType().getValue());
        bind(program.getUniformID(ShaderLightUniform.Enabled.getUniformName(lightIndex)), light.isEnabled());
        bind(program.getUniformID(ShaderLightUniform.CastShadows.getUniformName(lightIndex)), light.isCastShadows());

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
