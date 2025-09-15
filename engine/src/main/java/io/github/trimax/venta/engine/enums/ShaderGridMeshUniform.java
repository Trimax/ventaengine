package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderGridMeshUniform {
    /* Surface parameters */
    Surface("surface"),
    Trough("trough"),
    Peak("peak");

    private final String uniformName;
}
