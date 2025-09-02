package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.lwjgl.opengl.GL13C.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureType {
    AmbientOcclusion("ambientOcclusion", GL_TEXTURE3, 3),
    Anisotropy("anisotropy", GL_TEXTURE6, 6),
    Diffuse("diffuse", GL_TEXTURE0, 0),
    Height("height", GL_TEXTURE1, 1),
    Metalness("metalness", GL_TEXTURE5, 5),
    Normal("normal", GL_TEXTURE2, 2),
    Roughness("roughness", GL_TEXTURE4, 4);

    private final String fieldName;
    private final int locationID;
    private final int unitID;
}

