package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderUniform {
    /* Matrices */
    MatrixViewProjection("matrixViewProjection"),
    MatrixProjection("matrixProjection"),
    MatrixNormal("matrixNormal"),
    MatrixModel("matrixModel"),
    MatrixView("matrixView"),

    /* Textures */
    TextureSkybox("textureSkybox"),
    TextureDiffuse("textureDiffuse"),
    TextureHeight("textureHeight"),
    TextureNormal("textureNormal"),
    TextureRoughness("textureRoughness"),
    TextureMetalness("textureMetalness"),
    TextureAmbientOcclusion("textureAmbientOcclusion"),

    /* Texture flags */
    UseTextureSkybox("useTextureSkybox"),
    UseTextureDiffuseFlag("useTextureDiffuse"),
    UseTextureHeight("useTextureHeight"),
    UseTextureNormal("useTextureNormal"),
    UseTextureRoughness("useTextureRoughness"),
    UseTextureMetalness("useTextureMetalness"),
    UseTextureAmbientOcclusion("useTextureAmbientOcclusion"),

    /* Materials */
    MaterialMetalness("material.metalness"),
    MaterialRoughness("material.roughness"),
    MaterialTiling("material.tiling"),
    MaterialOffset("material.offset"),
    MaterialColor("material.color"),

    /* Material flags */
    UseMaterial("useMaterial"),

    /* Camera */
    CameraPosition("cameraPosition"),

    /* Lighting */
    AmbientLight("ambientLight"),
    UseLighting("useLighting"),
    LightCount("lightCount"),

    /* Fog */
    FogColor("fog.color"),
    FogMinimalDistance("fog.minimalDistance"),
    FogMaximalDistance("fog.maximalDistance"),

    /* Fog flag */
    UseFogFlag("useFog"),

    /* Common model parameters transformation*/
    Position("position"),
    Scale("scale"),
    Color("color");

    private final String uniformName;
}
