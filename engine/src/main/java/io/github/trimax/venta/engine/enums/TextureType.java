package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.lwjgl.opengl.GL13C.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureType {
    AmbientOcclusion("ambientOcclusion", GL_TEXTURE4, 4),
    Anisotropy("anisotropy", GL_TEXTURE7, 7),
    Diffuse("diffuse", GL_TEXTURE1, 1),
    Height("height", GL_TEXTURE2, 2),
    Metalness("metalness", GL_TEXTURE6, 6),
    Normal("normal", GL_TEXTURE3, 3),
    Roughness("roughness", GL_TEXTURE5, 5);

    private final String fieldName;
    private final int locationID;
    private final int unitID;
}

