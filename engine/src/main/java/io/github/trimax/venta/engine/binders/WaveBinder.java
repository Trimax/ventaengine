package io.github.trimax.venta.engine.binders;

import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.ShaderWaveUniform;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaveBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final List<Wave> waves) {
        final var wavesCount = waves != null ? waves.size() : 0;

        bind(program.getUniformID(ShaderUniform.WaveCount), wavesCount);
        for (int waveID = 0; waveID < wavesCount; waveID++)
            bind(program, waves.get(waveID), waveID);
    }

    private void bind(final ProgramEntityImplementation program, final Wave wave, final int waveIndex) {
        bind(program.getUniformID(ShaderWaveUniform.Direction.getUniformName(waveIndex)), wave.direction());
        bind(program.getUniformID(ShaderWaveUniform.Amplitude.getUniformName(waveIndex)), wave.amplitude());
        bind(program.getUniformID(ShaderWaveUniform.Steepness.getUniformName(waveIndex)), wave.steepness());
        bind(program.getUniformID(ShaderWaveUniform.Length.getUniformName(waveIndex)), wave.length());
        bind(program.getUniformID(ShaderWaveUniform.Speed.getUniformName(waveIndex)), wave.speed());
    }


    public void bind(final ProgramEntityImplementation program, final Wave wave) {
        bind(program.getUniformID(ShaderWaveUniform.Direction.getUniformName()), wave.direction());
        bind(program.getUniformID(ShaderWaveUniform.Amplitude.getUniformName()), wave.amplitude());
        bind(program.getUniformID(ShaderWaveUniform.Frequency.getUniformName()), wave.frequency());
        bind(program.getUniformID(ShaderWaveUniform.Steepness.getUniformName()), wave.steepness());
        bind(program.getUniformID(ShaderWaveUniform.Persistence.getUniformName()), wave.persistence());
        bind(program.getUniformID(ShaderWaveUniform.Lacunarity.getUniformName()), wave.lacunarity());
        bind(program.getUniformID(ShaderWaveUniform.Length.getUniformName()), wave.length());
        bind(program.getUniformID(ShaderWaveUniform.Speed.getUniformName()), wave.speed());
        bind(program.getUniformID(ShaderWaveUniform.Iterations.getUniformName()), wave.iterations());
    }
}