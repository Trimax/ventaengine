package io.github.trimax.venta.engine.enums;

import static org.lwjgl.opengl.GL13C.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
    Debug(GL_TEXTURE7, 7),
    Shadow(GL_TEXTURE8, 8),
    Irradiance(GL_TEXTURE9, 9),
    Prefilter(GL_TEXTURE10, 10),
    Brdf(GL_TEXTURE11, 11),
    PointShadow(GL_TEXTURE12, 12),
    SceneColor(GL_TEXTURE13, 13),
    SceneDepth(GL_TEXTURE14, 14);

    private final int locationID;
    private final int id;
}