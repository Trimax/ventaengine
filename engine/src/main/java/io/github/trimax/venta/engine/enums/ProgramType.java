package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProgramType {
    Particle("particle.json"),
    Default("default.json"),
    Console("console.json"),
    Simple("simple.json"),
    Text("text.json"),
    Sky("sky.json");

    private final String programName;
}
