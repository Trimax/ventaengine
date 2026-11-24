#version 330 core

#include "common/ambient-occlusion.glsl"

/*
 * Blurs the occlusion along one axis: the noise of the samples turns into a smooth shade. A sample counts the less, the
 * farther its distance from the eye is from the one of the pixel, so the shade of the floor doesn't leak onto a wall.
 */

/* The occlusion (red) and the distances (green) of half the size of the scene, and the step of the blur: one texel along an axis */
uniform sampler2D textureAo;
uniform vec2 aoBlurStep;

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* Output color */
out vec4 outputColor;

void main() {
    vec2 center = texture(textureAo, vertexTextureCoordinates).rg;

    float sum = 0.0;
    float total = 0.0;
    for (int i = -2; i <= 2; i++) {
        vec2 sampled = texture(textureAo, vertexTextureCoordinates + aoBlurStep * float(i)).rg;
        float weight = (i == 0 ? 6.0 : (abs(i) == 1 ? 4.0 : 1.0)) * getAoWeight(center.g, sampled.g);

        sum += sampled.r * weight;
        total += weight;
    }

    outputColor = vec4(sum / total, center.g, 0.0, 1.0);
}
