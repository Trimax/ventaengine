package com.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureType {
    AMBIENT_OCCLUSION("ambientOcclusion"),
    ANISOTROPY("anisotropy"),
    DIFFUSE("diffuse"),
    HEIGHT("height"),
    METALLIC("metallic"),
    NORMAL("normal"),
    ROUGHNESS("roughness");

    private final String fieldName;
}

