package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderUniform {
    /* Matrices */
    MatrixViewProjection("matrixViewProjection"),
    MatrixProjection("matrixProjection"),
    MatrixNormal("matrixNormal"),
    MatrixModel("matrixModel"),
    MatrixView("matrixView"),

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
    MaterialShininess("materialShininess"),
    MaterialTiling("materialTiling"),
    MaterialOffset("materialOffset"),
    MaterialColor("materialColor"),

    /* Material flags */
    UseMaterial("useMaterial"),

    /* Camera */
    CameraPosition("cameraPosition"),

    /* Lighting */
    AmbientLight("ambientLight"),
    UseLighting("useLighting"),
    LightCount("lightCount"),

    /* Fog */
    FogColor("fog.color"),
    FogDensity("fog.density"),

    /* Fog flag */
    UseFogFlag("useFog"),

    /* Common model parameters transformation*/
    Position("position"),
    Scale("scale"),
    Color("color");

    private final String uniformName;
}
