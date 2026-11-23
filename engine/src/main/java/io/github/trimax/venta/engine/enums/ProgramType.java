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
    ShadowCutout("shadow-cutout.json"),
    Particle("particle.json"),
    Post("post.json"),
    Default("default.json"),
    Console("console.json"),
    Shadow("shadow.json"),
    Simple("simple.json"),
    Text("text.json"),
    Sky("sky.json");

    private final String programName;
}
