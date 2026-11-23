#version 330 core

#include "common/fog.glsl"

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec2 vertexTextureCoordinates;
in vec4 vertexColor;
in float vertexFog;

/* Textures */
uniform sampler2D textureDiffuse;

/* Output color */
out vec4 outputColor;

void main() {
    vec4 color = texture(textureDiffuse, vertexTextureCoordinates) * vertexColor;

    /* The alpha is kept: a billboard in the fog keeps its shape and takes the fog color */
    outputColor = vec4(mix(color.rgb, fog.color, vertexFog), color.a);
}
