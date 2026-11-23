#version 330 core

#include "common/ibl.glsl"

/*
 * Draws the lookup table of the BRDF: the angle of the viewer (the cosine) goes along x, the roughness along y,
 * the scale and the bias of the fresnel are the red and the green channels.
 */
in vec2 vertexTextureCoordinates;

uniform int sampleCount;

out vec4 outputColor;

void main() {
    // A grazing viewer and a mirror give degenerate directions
    float NdotV = max(vertexTextureCoordinates.x, 0.001);
    float roughness = max(vertexTextureCoordinates.y, ROUGHNESS_MINIMAL);

    outputColor = vec4(integrateBrdf(NdotV, roughness, sampleCount), 0.0, 1.0);
}
