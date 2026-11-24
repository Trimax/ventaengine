package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.utils.ColorUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FogBinder extends AbstractBinder {
    /** Shaders get a finite height: an infinite fog layer is a very high one */
    private static final float MAXIMAL_HEIGHT = 1e30f;

    /**
     * Binds the fog.
     *
     * @param light the sun of the scene: the fog takes its color when it is seen towards the sun, null for a scene without it
     */
    public void bind(final ProgramEntityImplementation program, final Fog fog, final DirectionalLight light) {
        bind(program.getUniformID(ShaderUniform.UseFogFlag), fog != null);

        if (fog == null)
            return;

        bind(program.getUniformID(ShaderUniform.FogMinimalDistance), fog.getMinimalDistance());
        bind(program.getUniformID(ShaderUniform.FogMaximalDistance), fog.getMaximalDistance());
        bind(program.getUniformID(ShaderUniform.FogHeight), Math.min(fog.getHeight(), MAXIMAL_HEIGHT));
        bindColor(program.getUniformID(ShaderUniform.FogColor), fog.getColor());

        bind(program.getUniformID(ShaderUniform.FogDensity), fog.getDensity());
        bind(program.getUniformID(ShaderUniform.FogFalloff), fog.getFalloff());
        bind(program.getUniformID(ShaderUniform.FogBaseHeight), fog.getBaseHeight());
        bindSun(program, fog, light);
    }

    /** The sun in the fog needs a sun: without one the fog keeps its color */
    private void bindSun(final ProgramEntityImplementation program, final Fog fog, final DirectionalLight light) {
        final var scattering = light == null ? 0.f : fog.getSunScattering();
        bind(program.getUniformID(ShaderUniform.FogSunScattering), scattering);

        if (scattering <= 0.f)
            return;

        // The light goes along its direction, the sun is where it comes from
        bind(program.getUniformID(ShaderUniform.FogSunDirection), light.getDirection().normalize(new Vector3f()).negate());
        bind(program.getUniformID(ShaderUniform.FogSunSharpness), fog.getSunSharpness());
        bind(program.getUniformID(ShaderUniform.FogSunColor),
                ColorUtil.toLinear(light.getColor().x()) * light.getIntensity(),
                ColorUtil.toLinear(light.getColor().y()) * light.getIntensity(),
                ColorUtil.toLinear(light.getColor().z()) * light.getIntensity());
    }
}
