package com.venta.engine.enums;

import com.venta.engine.definitions.Definitions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderLightUniform {
    /* Basic parameters  */
    Type("type"),
    Color("color"),
    Enabled("enabled"),
    Position("position"),
    Direction("direction"),
    Intensity("intensity"),

    /* Attenuation */
    AttenuationLinear("attenuation.linear"),
    AttenuationConstant("attenuation.constant"),
    AttenuationQuadratic("attenuation.quadratic"),

    /* Shadows */
    CastShadows("castShadows");

    private final String uniformName;

    public String getUniformName(final int lightID) {
        return uniformNames.get(this).get(lightID);
    }

    private static final Map<ShaderLightUniform, Map<Integer, String>> uniformNames = new HashMap<>();

    static {
        for (final ShaderLightUniform uniform : ShaderLightUniform.values()) {
            uniformNames.putIfAbsent(uniform, new HashMap<>());
            for (int lightID = 0; lightID < Definitions.LIGHT_MAX; lightID++)
                uniformNames.get(uniform).put(lightID, String.format("lights[%d].%s", lightID, uniform.uniformName));
        }
    }
}
