package io.github.trimax.venta.engine.enums;

import io.github.trimax.venta.engine.definitions.Definitions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderWaveUniform {
    Direction("direction"),
    Amplitude("amplitude"),
    Steepness("steepness"),
    Length("length"),
    Speed("speed");

    private final String uniformName;

    public String getUniformName(final int waveID) {
        return uniformNames.get(this).get(waveID);
    }

    private static final Map<ShaderWaveUniform, Map<Integer, String>> uniformNames = new HashMap<>();

    static {
        for (final ShaderWaveUniform uniform : ShaderWaveUniform.values()) {
            uniformNames.putIfAbsent(uniform, new HashMap<>());
            for (int waveID = 0; waveID < Definitions.WAVE_MAX; waveID++)
                uniformNames.get(uniform).put(waveID, String.format("waves[%d].%s", waveID, uniform.uniformName));
        }
    }
}
