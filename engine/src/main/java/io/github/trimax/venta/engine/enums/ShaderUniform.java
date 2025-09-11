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
    TextureDebug("textureDebug"),

    /* Texture flags */
    UseTextureSkybox("useTextureSkybox"),
    UseTextureDiffuse("useTextureDiffuse"),
    UseTextureHeight("useTextureHeight"),
    UseTextureNormal("useTextureNormal"),
    UseTextureRoughness("useTextureRoughness"),
    UseTextureMetalness("useTextureMetalness"),
    UseTextureAmbientOcclusion("useTextureAmbientOcclusion"),
    UseTextureDebug("useTextureDebug"),

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

    /* Waves */
    WaveCount("waveCount"),

    /* Time */
    TimeElapsed("timeElapsed"),
    TimeDelta("timeDelta"),

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
