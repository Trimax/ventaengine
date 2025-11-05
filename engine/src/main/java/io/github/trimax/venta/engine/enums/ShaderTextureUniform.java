package io.github.trimax.venta.engine.enums;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderTextureUniform {
    Diffuse("textureDiffuse"),
    Height("textureHeight"),
    Normal("textureNormal"),
    Roughness("textureRoughness"),
    Metalness("textureMetalness"),
    AmbientOcclusion("textureAmbientOcclusion");

    private final String uniformName;

    public String getUniformName(final int materialID) {
        return uniformNames.get(this).get(materialID);
    }

    private static final Map<ShaderTextureUniform, Map<Integer, String>> uniformNames = new HashMap<>();

    static {
        for (final ShaderTextureUniform uniform : ShaderTextureUniform.values()) {
            uniformNames.putIfAbsent(uniform, new HashMap<>());
            for (int materialID = 0; materialID < DefinitionsCommon.MAX_MATERIALS; materialID++)
                uniformNames.get(uniform).put(materialID, String.format("%s[%d]", uniform.uniformName, materialID));
        }
    }
}
