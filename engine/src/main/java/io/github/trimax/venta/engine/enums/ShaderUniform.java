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
    MaterialOpacity("material.opacity"),
    MaterialTiling("material.tiling"),
    MaterialOffset("material.offset"),
    MaterialColor("material.color"),

    /* Water material parameters */
    MaterialColorSurface("material.colorSurface"),
    MaterialColorDepth("material.colorDepth"),
    MaterialColorPeak("material.colorPeak"),

    /* Material flags */
    UseMaterial("useMaterial"),

    /* Camera */
    CameraPosition("cameraPosition"),

    /* Lighting */
    AmbientLight("ambientLight"),
    UseLighting("useLighting"),
    LightCount("lightCount"),

    /* Directional light */
    LightDirectionalDirection("directionalLight.direction"),
    LightDirectionalIntensity("directionalLight.intensity"),
    LightDirectionalColor("directionalLight.color"),

    /* Use directional light flag */
    UseLightDirectionalFlag("useDirectionalLight"),

    /* Waves */
    WaveCount("waveCount"),
    WaveAmplitude("waveAmplitude"),

    /* Noises */
    NoiseCount("noiseCount"),

    /* Time */
    TimeElapsed("timeElapsed"),
    TimeDelta("timeDelta"),

    /* Fog */
    FogColor("fog.color"),
    FogMinimalDistance("fog.minimalDistance"),
    FogMaximalDistance("fog.maximalDistance"),

    /* Fog flag */
    UseFogFlag("useFog"),

    /* Billboard parameters */
    Frames("frames"),
    FrameIndex("frameIndex"),

    /* Bounds for offsets */
    BoundsPosition("boundsPosition"),
    BoundsTextureCoordinates("boundsTextureCoordinates"),

    /* Foam */
    FoamThreshold("foam.threshold"),
    FoamIntensity("foam.intensity"),

    /* Foam flags */
    UseFoam("useFoam"),

    /* Common model parameters transformation */
    Color("color");

    private final String uniformName;
}
