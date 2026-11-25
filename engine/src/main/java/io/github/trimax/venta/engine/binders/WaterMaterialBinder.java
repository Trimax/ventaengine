package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.water.WaterMaterial;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterMaterialBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final WaterMaterial material) {
        bind(program.getUniformID(ShaderUniform.UseMaterial), material != null);

        if (material == null)
            return;

        bind(program.getUniformID(ShaderUniform.MaterialMetalness), material.getMetalness());
        bind(program.getUniformID(ShaderUniform.MaterialOpacity), material.getOpacity());
        bind(program.getUniformID(ShaderUniform.MaterialDepthDistance), material.getDepthDistance());
        bind(program.getUniformID(ShaderUniform.MaterialRefraction), material.getRefraction());
        bind(program.getUniformID(ShaderUniform.WaveFadeDistance), material.getWaveFadeDistance());
        bind(program.getUniformID(ShaderUniform.SwashAmplitude), material.getSwashAmplitude());
        bind(program.getUniformID(ShaderUniform.SwashPeriod), material.getSwashPeriod());
        bindColor(program.getUniformID(ShaderUniform.MaterialColorSurface), material.getColorSurface());
        bindColor(program.getUniformID(ShaderUniform.MaterialColorDepth), material.getColorDepth());
        bindColor(program.getUniformID(ShaderUniform.MaterialColorPeak), material.getColorPeak());
    }
}
