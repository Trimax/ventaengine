package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderUniform {
    /* Matrices */
    MatrixViewProjection("matrixViewProjection"),
    MatrixNormal("matrixNormal"),
    MatrixModel("matrixModel"),

    /* Textures */
    TextureDiffuse("textureDiffuse"),
    TextureHeight("textureHeight"),
    TextureNormal("textureNormal"),
    TextureRoughness("textureRoughness"),
    TextureAmbientOcclusion("textureAmbientOcclusion"),

    /* Texture flags */
    UseTextureDiffuseFlag("useTextureDiffuse"),
    UseTextureHeight("useTextureHeight"),
    UseTextureNormal("useTextureNormal"),
    UseTextureRoughness("useTextureRoughness"),
    UseTextureAmbientOcclusion("useTextureAmbientOcclusion"),

    /* Materials */
    MaterialTiling("materialTiling"),

    /* Camera */
    CameraPosition("cameraPosition"),

    /* Lighting */
    AmbientLight("ambientLight"),
    UseLighting("useLighting"),
    LightCount("lightCount"),

    /* Common model parameters transformation*/
    Position("position"),
    Scale("scale"),
    Color("color");

    private final String uniformName;
}
