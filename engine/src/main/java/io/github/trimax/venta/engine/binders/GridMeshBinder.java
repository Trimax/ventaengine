package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderGridMeshUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program) {
        bind(program.getUniformID(ShaderGridMeshUniform.WavesAmplitude.getUniformName()), 0.018f);
        bind(program.getUniformID(ShaderGridMeshUniform.WavesSpeed.getUniformName()), 0.15f);
        bind(program.getUniformID(ShaderGridMeshUniform.WavesFrequency.getUniformName()), 1.5f);
        bind(program.getUniformID(ShaderGridMeshUniform.WavesPersistence.getUniformName()), 0.32f);
        bind(program.getUniformID(ShaderGridMeshUniform.WavesLacunarity.getUniformName()), 2.32f);
        bind(program.getUniformID(ShaderGridMeshUniform.WavesIterations.getUniformName()), 8.f);


        bind(program.getUniformID(ShaderGridMeshUniform.Opacity.getUniformName()), 1.f);


        bind(program.getUniformID(ShaderGridMeshUniform.ColorTrough.getUniformName()), new Vector3f(0.01f, 0.13f, 0.28f));
        bind(program.getUniformID(ShaderGridMeshUniform.ColorSurface.getUniformName()), new Vector3f(0.12f, 0.52f, 0.34f));
        bind(program.getUniformID(ShaderGridMeshUniform.ColorPeak.getUniformName()), new Vector3f(0.50f, 0.59f, 0.75f));


        bind(program.getUniformID(ShaderGridMeshUniform.ThresholdPeak.getUniformName()), 0.20f);
        bind(program.getUniformID(ShaderGridMeshUniform.ThresholdTrough.getUniformName()), 0.0f);


        bind(program.getUniformID(ShaderGridMeshUniform.TransitionPeak.getUniformName()), 0.12f);
        bind(program.getUniformID(ShaderGridMeshUniform.TransitionTrough.getUniformName()), 0.21f);


        bind(program.getUniformID(ShaderGridMeshUniform.FresnelScale.getUniformName()), 0.51f);
        bind(program.getUniformID(ShaderGridMeshUniform.FresnelPower.getUniformName()), 1.30f);



    }
}
