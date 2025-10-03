package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureType {
    AmbientOcclusion("ambientOcclusion", TextureUnit.AmbientOcclusion, ShaderUniform.TextureAmbientOcclusion, ShaderUniform.UseTextureAmbientOcclusion),
    Diffuse("diffuse", TextureUnit.Diffuse, ShaderUniform.TextureDiffuse, ShaderUniform.UseTextureDiffuse),
    Height("height", TextureUnit.Height, ShaderUniform.TextureHeight, ShaderUniform.UseTextureHeight),
    Metalness("metalness", TextureUnit.Metalness, ShaderUniform.TextureMetalness, ShaderUniform.UseTextureMetalness),
    Normal("normal", TextureUnit.Normal, ShaderUniform.TextureNormal, ShaderUniform.UseTextureNormal),
    Roughness("roughness", TextureUnit.Roughness, ShaderUniform.TextureRoughness, ShaderUniform.UseTextureRoughness),


    Color("color", TextureUnit.Color, ShaderUniform.TextureColor, ShaderUniform.UseTextureColor),
    ARMS("arms", TextureUnit.ARMS, ShaderUniform.TextureARMS, ShaderUniform.UseTextureARMS),
    NH("nh", TextureUnit.NH, ShaderUniform.TextureNH, ShaderUniform.UseTextureNH);

    private final String fieldName;
    private final TextureUnit unit;
    private final ShaderUniform textureUniform;
    private final ShaderUniform useTextureUniform;
}

