package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProgramType {
    Particle("particle.json"),
    Simple("simple.json"),
    Basic("basic.json");

    private final String programName;
}
