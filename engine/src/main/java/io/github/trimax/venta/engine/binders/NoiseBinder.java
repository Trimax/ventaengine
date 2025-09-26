package io.github.trimax.venta.engine.binders;

import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderNoiseUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.shared.Noise;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoiseBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final List<Noise> noises) {
        final var noisesCount = noises != null ? noises.size() : 0;

        bind(program.getUniformID(ShaderUniform.NoiseCount), noisesCount);
        for (int noiseID = 0; noiseID < noisesCount; noiseID++)
            bind(program, noises.get(noiseID), noiseID);
    }

    private void bind(final ProgramEntityImplementation program, final Noise noise, final int index) {
        bind(program.getUniformID(ShaderNoiseUniform.Scale.getUniformName(index)), noise.getScale());
        bind(program.getUniformID(ShaderNoiseUniform.Speed.getUniformName(index)), noise.getSpeed());
        bind(program.getUniformID(ShaderNoiseUniform.Offset.getUniformName(index)), noise.getOffset());
        bind(program.getUniformID(ShaderNoiseUniform.Strength.getUniformName(index)), noise.getStrength());
    }
}
