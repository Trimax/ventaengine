#version 330 core

#include "common/ibl.glsl"

/*
 * Draws a face of the environment map: the skybox copied into a floating point cube map of a fixed size, which gets
 * mip levels (the later passes read the sky blurred from them).
 */
in vec2 vertexTextureCoordinates;

uniform samplerCube textureSource;
uniform int face;

out vec4 outputColor;

void main() {
    vec3 direction = getCubeDirection(face, vertexTextureCoordinates * 2.0 - 1.0);

    outputColor = vec4(textureLod(textureSource, direction, 0.0).rgb, 1.0);
}
