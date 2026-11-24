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
    TextureScene("textureScene"),
    TextureBloom("textureBloom"),
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
    MaterialAlphaCutoff("material.alphaCutoff"),

    /* Count of materials bound */
    MaterialCount("materialCount"),

    /* Water material parameters */
    MaterialColorSurface("material.colorSurface"),
    MaterialColorDepth("material.colorDepth"),
    MaterialColorPeak("material.colorPeak"),

    /* Material flags */
    UseMaterial("useMaterial"),
    UseAlphaCutout("useAlphaCutout"),
    UseAlphaOpaque("useAlphaOpaque"),

    /* Camera */
    CameraPosition("cameraPosition"),
    CameraForward("cameraForward"),

    /* Shadows */
    UseShadows("useShadows"),
    TextureShadow("textureShadow"),
    ShadowCascadeCount("shadowCascadeCount"),
    ShadowMatrices("shadowMatrices"),
    ShadowSplits("shadowSplits"),
    ShadowTexels("shadowTexels"),
    ShadowDistance("shadowDistance"),

    /* Environment (image based lighting) */
    UseEnvironment("useEnvironment"),
    TextureIrradiance("textureIrradiance"),
    TexturePrefilter("texturePrefilter"),
    TextureBrdf("textureBrdf"),
    PrefilterLevels("prefilterLevels"),

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

    /* Wind: the wind of the scene, how much the material follows it, and the bounding box of the mesh */
    WindDirection("windDirection"),
    WindStrength("windStrength"),
    WindSpeed("windSpeed"),
    WindInfluence("windInfluence"),
    MeshMinimum("meshMinimum"),
    MeshMaximum("meshMaximum"),

    /* The disc of the sun in the sky */
    UseSunDisc("useSunDisc"),
    SunDirection("sunDirection"),
    SunColor("sunColor"),
    SunSize("sunSize"),

    /* Fog */
    /* Post processing */
    BloomIntensity("bloomIntensity"),
    UseBloom("useBloom"),
    Exposure("exposure"),
    ToneMapping("toneMapping"),

    FogColor("fog.color"),
    FogMinimalDistance("fog.minimalDistance"),
    FogMaximalDistance("fog.maximalDistance"),
    FogHeight("fog.height"),
    FogDensity("fog.density"),
    FogFalloff("fog.falloff"),
    FogBaseHeight("fog.baseHeight"),
    FogSunDirection("fog.sunDirection"),
    FogSunColor("fog.sunColor"),
    FogSunScattering("fog.sunScattering"),
    FogSunSharpness("fog.sunSharpness"),

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

    Elevations("elevations"),
    GridSize("gridSize"),

    /* Common model parameters transformation */
    Factor("factor"),
    Color("color");

    private final String uniformName;
}
