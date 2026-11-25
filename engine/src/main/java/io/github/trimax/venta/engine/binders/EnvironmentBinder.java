package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.renderers.common.EnvironmentRenderer;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

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

    /**
     * Makes the object that is drawn next reflect a cube map of its surroundings instead of the sky: the shader reads the prefiltered
     * map and the skybox from their units, so the cube map takes both (its mip levels are the blur of the roughness).
     *
     * @param levels the number of the mip levels of the cube map
     */
    public void bindProbe(final ProgramEntityImplementation program, final int cubeID, final int levels) {
        glActiveTexture(TextureUnit.Prefilter.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);
        glActiveTexture(TextureUnit.Skybox.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);

        bind(program.getUniformID(ShaderUniform.PrefilterLevels), (float) levels);
    }

    /** The objects that are drawn next reflect the sky again (the skybox is bound by every object itself) */
    public void unbindProbe(final ProgramEntityImplementation program) {
        final var environment = environmentRenderer.getEnvironment();
        if (environment == null)
            return;

        glActiveTexture(TextureUnit.Prefilter.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, environment.prefilterID());
        glActiveTexture(TextureUnit.Skybox.getLocationID());

        bind(program.getUniformID(ShaderUniform.PrefilterLevels), (float) environment.prefilterLevels());
    }
}
