#version 330 core

#include "common/ibl.glsl"
#include "common/panorama.glsl"

/*
 * Draws a face of a cube map from a panorama of the sky: the color of every direction of the face is read from the panorama.
 * The colors are the light of the sky, more than 1 near the sun, and are kept at the clamp at most.
 */
in vec2 vertexTextureCoordinates;

uniform sampler2D texturePanorama;
uniform int face;
uniform float clampValue;

out vec4 outputColor;

void main() {
    vec3 direction = getCubeDirection(face, vertexTextureCoordinates * 2.0 - 1.0);

    outputColor = vec4(min(textureLod(texturePanorama, getPanoramaCoordinates(direction), 0.0).rgb, vec3(clampValue)), 1.0);
}
