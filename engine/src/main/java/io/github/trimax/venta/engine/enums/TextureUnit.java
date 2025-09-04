package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.lwjgl.opengl.GL13C.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureUnit {
    Skybox(GL_TEXTURE0, 0),
    Diffuse(GL_TEXTURE1, 1),
    Height(GL_TEXTURE2, 2),
    Normal(GL_TEXTURE3, 3),
    AmbientOcclusion(GL_TEXTURE4, 4),
    Roughness(GL_TEXTURE5, 5),
    Metalness(GL_TEXTURE6, 6),
    Debug(GL_TEXTURE7, 7);

    private final int locationID;
    private final int id;
}