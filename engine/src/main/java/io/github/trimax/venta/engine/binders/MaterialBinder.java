package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderMaterialUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL13C.GL_SAMPLE_ALPHA_TO_COVERAGE;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialBinder extends AbstractBinder {
    private final TextureBinder textureBinder;

    public void bind(final ProgramEntityImplementation program, final MaterialEntityImplementation material) {
        bind(program.getUniformID(ShaderUniform.UseMaterial), material != null);
        bind(program.getUniformID(ShaderUniform.UseAlphaCutout), material != null && material.isCutout());
        bind(program.getUniformID(ShaderUniform.UseAlphaOpaque), material != null && material.isOpaque());

        // Uniforms are per program state, so textures of a previously rendered material must be reset
        StreamEx.of(TextureType.values()).forEach(type -> bind(type, program, material));

        if (material == null)
            return;

        bind(program.getUniformID(ShaderUniform.MaterialMetalness), material.getMetalness());
        bind(program.getUniformID(ShaderUniform.MaterialRoughness), material.getRoughness());
        bind(program.getUniformID(ShaderUniform.MaterialAlphaCutoff), material.getAlphaCutoff());
        bind(program.getUniformID(ShaderUniform.MaterialEmissive), material.getEmissive());
        bind(program.getUniformID(ShaderUniform.MaterialTiling), material.getTiling());
        bind(program.getUniformID(ShaderUniform.MaterialOffset), material.getOffset());
        bindColor(program.getUniformID(ShaderUniform.MaterialColor), material.getColor());
    }

    public void bind(final ProgramEntityImplementation program, final List<MaterialEntityImplementation> materials) {
        bind(program.getUniformID(ShaderUniform.MaterialCount), materials.size());

        int materialID = 0;
        for (final var material : materials)
            bind(program, material, materialID++);
    }

    private void bind(final ProgramEntityImplementation program, final MaterialEntityImplementation material, final int materialID) {
        bind(program.getUniformID(ShaderUniform.UseMaterial), material != null);

        if (material == null)
            return;

        bind(program.getUniformID(ShaderMaterialUniform.Metalness.getUniformName(materialID)), material.getMetalness());
        bind(program.getUniformID(ShaderMaterialUniform.Roughness.getUniformName(materialID)), material.getRoughness());
        bind(program.getUniformID(ShaderMaterialUniform.Tiling.getUniformName(materialID)), material.getTiling());
        bind(program.getUniformID(ShaderMaterialUniform.Offset.getUniformName(materialID)), material.getOffset());
        bindColor(program.getUniformID(ShaderMaterialUniform.Color.getUniformName(materialID)), material.getColor());
    }

    private void bind(final TextureType type, final ProgramEntityImplementation program, final MaterialEntityImplementation material) {
        textureBinder.bind(type, program, material != null ? material.getTexture(type) : null);
    }

    /**
     * Whether the holes of the cutout surface that is drawn next are smoothed by alpha to coverage: the shader outputs the share of
     * the samples that a fragment covers, so the colors must not be mixed by the alpha too (the blending is off until
     * {@link #unbindCoverage(boolean)}). Only a multisampled framebuffer has samples to cover.
     *
     * @param enabled whether the framebuffer is multisampled and the material has holes
     */
    public void bindCoverage(final ProgramEntityImplementation program, final boolean enabled) {
        bind(program.getUniformID(ShaderUniform.UseAlphaToCoverage), enabled);

        if (enabled) {
            glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE);
            glDisable(GL_BLEND);
        }
    }

    /**
     * What was changed by {@link #bindCoverage(ProgramEntityImplementation, boolean)} is as it was: the scene mixes colors by default.
     */
    public void unbindCoverage(final boolean enabled) {
        if (enabled) {
            glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE);
            glEnable(GL_BLEND);
        }
    }
}
