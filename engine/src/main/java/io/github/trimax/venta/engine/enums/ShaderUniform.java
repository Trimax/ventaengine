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
    MatrixViewProjectionInverse("matrixViewProjectionInverse"),

    /* Textures */
    TextureSkybox("textureSkybox"),
    TextureScene("textureScene"),
    TextureBloom("textureBloom"),
    TexturePointShadow("texturePointShadow"),
    TextureDepth("textureDepth"),
    TextureShafts("textureShafts"),
    TextureAo("textureAo"),
    TextureDiffuse("textureDiffuse"),
    TextureHeight("textureHeight"),
    TextureNormal("textureNormal"),
    TextureRoughness("textureRoughness"),
    TextureMetalness("textureMetalness"),
    TextureAmbientOcclusion("textureAmbientOcclusion"),
    TextureEmissive("textureEmissive"),
    TextureDebug("textureDebug"),

    /* Texture flags */
    UseTextureSkybox("useTextureSkybox"),
    UseTextureDiffuse("useTextureDiffuse"),
    UseTextureHeight("useTextureHeight"),
    UseTextureNormal("useTextureNormal"),
    UseTextureRoughness("useTextureRoughness"),
    UseTextureMetalness("useTextureMetalness"),
    UseTextureAmbientOcclusion("useTextureAmbientOcclusion"),
    UseTextureEmissive("useTextureEmissive"),
    UseTextureDebug("useTextureDebug"),

    /* Materials */
    MaterialMetalness("material.metalness"),
    MaterialRoughness("material.roughness"),
    MaterialOpacity("material.opacity"),
    MaterialDepthDistance("material.depthDistance"),
    MaterialRefraction("material.refraction"),
    WaveFadeDistance("waveFadeDistance"),
    SwashAmplitude("swashAmplitude"),
    SwashPeriod("swashPeriod"),
    MaterialTiling("material.tiling"),
    MaterialOffset("material.offset"),
    MaterialColor("material.color"),
    MaterialAlphaCutoff("material.alphaCutoff"),
    MaterialEmissive("material.emissive"),

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
    PointShadowMatrices("pointShadowMatrices"),
    UsePointShadows("usePointShadows"),
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
    AoRadius("aoRadius"),
    AoPower("aoPower"),
    AoIntensity("aoIntensity"),
    AoProjectionScale("aoProjectionScale"),
    AoBlurStep("aoBlurStep"),
    LightShaftsSteps("lightShaftsSteps"),
    LightShaftsAnisotropy("lightShaftsAnisotropy"),
    LightShaftsIntensity("lightShaftsIntensity"),
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
    FoamShoreWidth("foam.shoreWidth"),
    FoamShoreIntensity("foam.shoreIntensity"),
    FoamBands("foam.bands"),

    /* Foam flags */
    UseFoam("useFoam"),

    TextureSceneColor("textureSceneColor"),
    TextureSceneDepth("textureSceneDepth"),
    UseSceneTextures("useSceneTextures"),
    TextureReflection("textureReflection"),
    UseReflection("useReflection"),
    MatrixViewProjectionReflection("matrixViewProjectionReflection"),
    SkirtDepth("skirtDepth"),
    UseWetness("useWetness"),
    WetnessLevel("wetness.level"),
    WetnessHeight("wetness.height"),
    WetnessDarkening("wetness.darkening"),
    WetnessGlossiness("wetness.glossiness"),
    WetnessSwashAmplitude("wetness.swashAmplitude"),
    WetnessSwashPeriod("wetness.swashPeriod"),
    Elevations("elevations"),
    HeightMaterialCount("heightMaterialCount"),
    SlopeCount("slopeCount"),
    SlopeMinimums("slopeMinimums"),
    SlopeMaximums("slopeMaximums"),
    SlopeBlends("slopeBlends"),
    WorldSize("worldSize"),
    GridSize("gridSize"),

    /* Common model parameters transformation */
    Factor("factor"),
    Color("color");

    private final String uniformName;
}
