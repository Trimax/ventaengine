package io.github.trimax.venta.engine.enums;

import java.util.HashMap;
import java.util.Map;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderNoiseUniform {
    Scale("scale"),
    Speed("speed"),
    Offset("offset"),
    Strength("strength");

    private final String uniformName;

    public String getUniformName(final int waveID) {
        return uniformNames.get(this).get(waveID);
    }

    private static final Map<ShaderNoiseUniform, Map<Integer, String>> uniformNames = new HashMap<>();

    static {
        for (final ShaderNoiseUniform uniform : ShaderNoiseUniform.values()) {
            uniformNames.putIfAbsent(uniform, new HashMap<>());
            for (int waveID = 0; waveID < DefinitionsCommon.MAX_NOISES; waveID++)
                uniformNames.get(uniform).put(waveID, String.format("noises[%d].%s", waveID, uniform.uniformName));
        }
    }
}
