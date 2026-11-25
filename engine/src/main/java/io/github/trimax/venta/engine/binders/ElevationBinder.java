package io.github.trimax.venta.engine.binders;

import java.util.List;

import org.joml.Vector2fc;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.terrain.TerrainSlope;
import io.github.trimax.venta.engine.model.common.terrain.TerrainWetness;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElevationBinder extends AbstractBinder {
    private final TextureBinder textureBinder;

    public void bind(final ProgramEntityImplementation program, final TextureEntityImplementation heightmap, final float[] elevations,
                     final float factor, final Vector2fc gridSize) {
        textureBinder.bind(TextureType.Height, program, heightmap);
        bind(program.getUniformID(ShaderUniform.Factor), factor);
        bind(program.getUniformID(ShaderUniform.GridSize), gridSize);

        bind(program.getUniformID(ShaderUniform.Elevations), elevations);
    }

    /**
     * Gives the shader the materials by the steepness of the ground, which are the last ones of the materials of the terrain.
     *
     * @param slopes    the materials by the steepness
     * @param worldSize the size of the terrain in the world: the textures that are sampled from the sides are laid by it
     */
    public void bind(final ProgramEntityImplementation program, final List<TerrainSlope> slopes, final int heightMaterials, final Vector2fc worldSize) {
        bind(program.getUniformID(ShaderUniform.HeightMaterialCount), heightMaterials);
        bind(program.getUniformID(ShaderUniform.SlopeCount), slopes.size());
        bind(program.getUniformID(ShaderUniform.WorldSize), worldSize);

        if (slopes.isEmpty())
            return;

        final var minimums = new float[slopes.size()];
        final var maximums = new float[slopes.size()];
        final var blends = new float[slopes.size()];
        for (int i = 0; i < slopes.size(); i++) {
            minimums[i] = slopes.get(i).getMinimum();
            maximums[i] = slopes.get(i).getMaximum();
            blends[i] = slopes.get(i).getBlend();
        }

        bind(program.getUniformID(ShaderUniform.SlopeMinimums), minimums);
        bind(program.getUniformID(ShaderUniform.SlopeMaximums), maximums);
        bind(program.getUniformID(ShaderUniform.SlopeBlends), blends);
    }

    /** How deep the skirts of the chunks of the terrain hang, in the local space of the terrain */
    public void bindSkirt(final ProgramEntityImplementation program, final float depth) {
        bind(program.getUniformID(ShaderUniform.SkirtDepth), depth);
    }

    /** Gives the shader the wet ground of a shore, or switches it off when there is none */
    public void bindWetness(final ProgramEntityImplementation program, final TerrainWetness wetness) {
        bind(program.getUniformID(ShaderUniform.UseWetness), wetness != null);

        if (wetness == null)
            return;

        bind(program.getUniformID(ShaderUniform.WetnessLevel), wetness.getLevel());
        bind(program.getUniformID(ShaderUniform.WetnessHeight), wetness.getHeight());
        bind(program.getUniformID(ShaderUniform.WetnessDarkening), wetness.getDarkening());
        bind(program.getUniformID(ShaderUniform.WetnessGlossiness), wetness.getGlossiness());
        bind(program.getUniformID(ShaderUniform.WetnessSwashAmplitude), wetness.getSwashAmplitude());
        bind(program.getUniformID(ShaderUniform.WetnessSwashPeriod), wetness.getSwashPeriod());
    }
}
