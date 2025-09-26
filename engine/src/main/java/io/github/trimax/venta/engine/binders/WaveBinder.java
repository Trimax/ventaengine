package io.github.trimax.venta.engine.binders;

import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.ShaderWaveUniform;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaveBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final float amplitude) {
        bind(program.getUniformID(ShaderUniform.WaveAmplitude), amplitude);
    }

    public void bind(final ProgramEntityImplementation program, final List<Wave> waves) {
        final var wavesCount = waves != null ? waves.size() : 0;

        bind(program.getUniformID(ShaderUniform.WaveCount), wavesCount);
        for (int waveID = 0; waveID < wavesCount; waveID++)
            bind(program, waves.get(waveID), waveID);
    }

    private void bind(final ProgramEntityImplementation program, final Wave wave, final int index) {
        bind(program.getUniformID(ShaderWaveUniform.Direction.getUniformName(index)), wave.getDirection());
        bind(program.getUniformID(ShaderWaveUniform.Amplitude.getUniformName(index)), wave.getAmplitude());
        bind(program.getUniformID(ShaderWaveUniform.Steepness.getUniformName(index)), wave.getSteepness());
        bind(program.getUniformID(ShaderWaveUniform.Length.getUniformName(index)), wave.getLength());
        bind(program.getUniformID(ShaderWaveUniform.Speed.getUniformName(index)), wave.getSpeed());
    }
}
