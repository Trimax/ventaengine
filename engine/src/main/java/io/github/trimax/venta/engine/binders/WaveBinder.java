package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.ShaderWaveUniform;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaveBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final List<Wave> waves) {
        bind(program.getUniformID(ShaderUniform.WaveCount), waves.size());
        for (int waveID = 0; waveID < waves.size(); waveID++)
            bind(program, waves.get(waveID), waveID);
    }

    private void bind(final ProgramEntityImplementation program, final Wave wave, final int waveIndex) {
        bind(program.getUniformID(ShaderWaveUniform.Direction.getUniformName(waveIndex)), wave.direction());
        bind(program.getUniformID(ShaderWaveUniform.Amplitude.getUniformName(waveIndex)), wave.amplitude());
        bind(program.getUniformID(ShaderWaveUniform.Steepness.getUniformName(waveIndex)), wave.steepness());
        bind(program.getUniformID(ShaderWaveUniform.Length.getUniformName(waveIndex)), wave.length());
        bind(program.getUniformID(ShaderWaveUniform.Speed.getUniformName(waveIndex)), wave.speed());
    }
}
