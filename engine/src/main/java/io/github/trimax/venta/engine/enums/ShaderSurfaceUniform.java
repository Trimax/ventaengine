package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderSurfaceUniform {
    Color("color"),
    Threshold("threshold"),
    Transition("transition");

    private final String uniformName;

    public String getUniformName(final String name) {
        uniformNames.get(this).computeIfAbsent(name, _ -> String.format("%s.%s", name, uniformName));

        return uniformNames.get(this).get(name);
    }

    private static final Map<ShaderSurfaceUniform, Map<String, String>> uniformNames = new HashMap<>();

    static {
        for (final ShaderSurfaceUniform uniform : ShaderSurfaceUniform.values())
            uniformNames.putIfAbsent(uniform, new HashMap<>());
    }
}
