package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderGridMeshUniform {
    WavesAmplitude("uWavesAmplitude"),
    WavesSpeed("uWavesSpeed"),
    WavesFrequency("uWavesFrequency"),
    WavesPersistence("uWavesPersistence"),
    WavesLacunarity("uWavesLacunarity"),
    WavesIterations("uWavesIterations"),

    ColorTrough("uTroughColor"),
    ColorSurface("uSurfaceColor"),
    ColorPeak("uPeakColor"),

    Opacity("uOpacity"),

    ThresholdPeak("uPeakThreshold"),
    ThresholdTrough("uTroughThreshold"),

    TransitionPeak("uPeakTransition"),
    TransitionTrough("uTroughTransition"),

    FresnelScale("uFresnelScale"),
    FresnelPower("uFresnelPower");

    private final String uniformName;
}
