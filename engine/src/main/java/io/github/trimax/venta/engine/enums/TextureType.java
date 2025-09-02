package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureType {
    AmbientOcclusion("ambientOcclusion", TextureUnit.AmbientOcclusion),
    Diffuse("diffuse", TextureUnit.Diffuse),
    Height("height", TextureUnit.Height),
    Metalness("metalness", TextureUnit.Metalness),
    Normal("normal", TextureUnit.Normal),
    Roughness("roughness", TextureUnit.Roughness);

    private final String fieldName;
    private final TextureUnit unit;
}

