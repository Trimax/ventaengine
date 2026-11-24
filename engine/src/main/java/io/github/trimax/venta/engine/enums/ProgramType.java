package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProgramType {
    EnvironmentIrradiance("ibl-irradiance.json"),
    EnvironmentPrefilter("ibl-prefilter.json"),
    EnvironmentCopy("ibl-environment.json"),
    EnvironmentBrdf("ibl-brdf.json"),
    ShadowTerrain("shadow-terrain.json"),
    ShadowCutoutInstanced("shadow-cutout-instanced.json"),
    ShadowInstanced("shadow-instanced.json"),
    Particle("particle.json"),
    Post("post.json"),
    DefaultCutoutInstanced("default-cutout-instanced.json"),
    DefaultInstanced("default-instanced.json"),
    DefaultCutout("default-cutout.json"),
    Default("default.json"),
    Console("console.json"),
    Simple("simple.json"),
    Text("text.json"),
    Sky("sky.json");

    private final String programName;
}
