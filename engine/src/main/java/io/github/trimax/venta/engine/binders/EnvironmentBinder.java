package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.renderers.common.EnvironmentRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gives the shaders the light of the environment of the scene: the maps are bound to their units by the renderer of
 * the environment, here the samplers and the parameters are set.
 */
@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentBinder extends AbstractBinder {
    private final EnvironmentRenderer environmentRenderer;

    public void bind(final ProgramEntityImplementation program) {
        final var environment = environmentRenderer.getEnvironment();

        // The samplers of different types must never share a unit, so they are set even without an environment
        bind(program.getUniformID(ShaderUniform.TextureIrradiance), TextureUnit.Irradiance.getId());
        bind(program.getUniformID(ShaderUniform.TexturePrefilter), TextureUnit.Prefilter.getId());
        bind(program.getUniformID(ShaderUniform.TextureBrdf), TextureUnit.Brdf.getId());
        bind(program.getUniformID(ShaderUniform.UseEnvironment), environment != null);

        if (environment == null)
            return;

        bind(program.getUniformID(ShaderUniform.PrefilterLevels), (float) environment.prefilterLevels());
    }
}
