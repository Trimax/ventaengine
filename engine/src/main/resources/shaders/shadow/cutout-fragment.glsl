#version 330 core

#include "common/flags.glsl"

/*
 * The shadow of a cutout material (leaves, fences): the fragments that are holes in the surface cast no shadow.
 * The alpha is computed as in the shader of the objects, so the shadow has the shape of what is seen.
 */
struct Material {
    vec4 color;
    vec2 tiling;
    vec2 offset;
    float alphaCutoff;
};

in vec2 vertexTextureCoordinates;
in vec4 vertexColor;

uniform sampler2D textureDiffuse;
uniform int useTextureDiffuse;
uniform int useMaterial;
uniform Material material;

void main() {
    vec2 textureCoordinates = isSet(useMaterial) ? vertexTextureCoordinates * material.tiling + material.offset : vertexTextureCoordinates;

    float alpha = vertexColor.a;
    if (isSet(useTextureDiffuse))
        alpha *= texture(textureDiffuse, textureCoordinates).a;

    if (isSet(useMaterial))
        alpha *= material.color.a;

    if (alpha < material.alphaCutoff)
        discard;
}
