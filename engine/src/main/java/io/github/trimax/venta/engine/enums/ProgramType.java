package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProgramType {
    AmbientOcclusionApply("ambient-occlusion-apply.json"),
    AmbientOcclusionBlur("ambient-occlusion-blur.json"),
    AmbientOcclusion("ambient-occlusion.json"),
    Fxaa("fxaa.json"),
    LightShaftsUpsample("light-shafts-upsample.json"),
    LightShafts("light-shafts.json"),
    BloomDownsample("bloom-downsample.json"),
    BloomUpsample("bloom-upsample.json"),
    EnvironmentIrradiance("ibl-irradiance.json"),
    EnvironmentPrefilter("ibl-prefilter.json"),
    EnvironmentCopy("ibl-environment.json"),
    EnvironmentBrdf("ibl-brdf.json"),
    ShadowTerrain("shadow-terrain.json"),
    ShadowCutoutInstanced("shadow-cutout-instanced.json"),
    ShadowInstanced("shadow-instanced.json"),
    Particle("particle.json"),
    Post("post.json"),
    DefaultCutoutInstancedFade("default-cutout-instanced-fade.json"),
    DefaultCutoutInstanced("default-cutout-instanced.json"),
    DefaultCutoutFacingFade("default-cutout-facing-fade.json"),
    DefaultCutoutFacing("default-cutout-facing.json"),
    DefaultInstancedFade("default-instanced-fade.json"),
    DefaultInstanced("default-instanced.json"),
    DefaultCutout("default-cutout.json"),
    Default("default.json"),
    Console("console.json"),
    Simple("simple.json"),
    Text("text.json"),
    Sky("sky.json");

    private final String programName;
}
