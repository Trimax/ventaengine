package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureType {
    AmbientOcclusion("ambientOcclusion", TextureUnit.AmbientOcclusion, ShaderUniform.TextureAmbientOcclusion, ShaderUniform.UseTextureAmbientOcclusion, ShaderTextureUniform.AmbientOcclusion),
    Diffuse("diffuse", TextureUnit.Diffuse, ShaderUniform.TextureDiffuse, ShaderUniform.UseTextureDiffuse, ShaderTextureUniform.Diffuse),
    Height("height", TextureUnit.Height, ShaderUniform.TextureHeight, ShaderUniform.UseTextureHeight, ShaderTextureUniform.Height),
    Metalness("metalness", TextureUnit.Metalness, ShaderUniform.TextureMetalness, ShaderUniform.UseTextureMetalness, ShaderTextureUniform.Metalness),
    Normal("normal", TextureUnit.Normal, ShaderUniform.TextureNormal, ShaderUniform.UseTextureNormal, ShaderTextureUniform.Normal),
    Roughness("roughness", TextureUnit.Roughness, ShaderUniform.TextureRoughness, ShaderUniform.UseTextureRoughness, ShaderTextureUniform.Roughness);

    private final String fieldName;
    private final TextureUnit unit;
    private final ShaderUniform textureUniform;
    private final ShaderUniform useTextureUniform;
    private final ShaderTextureUniform stackedTextureUniform;
}

