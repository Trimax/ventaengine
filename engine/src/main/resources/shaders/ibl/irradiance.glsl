#version 330 core

#include "common/ibl.glsl"

/*
 * Draws a face of the irradiance map: for every direction the sky around it weighted by the cosine of the angle,
 * that is what a surface facing the direction diffuses. A sky of one color gives that color.
 * The sky is read from a coarse mip level: the irradiance changes slowly, so the few samples stay smooth.
 */
in vec2 vertexTextureCoordinates;

uniform samplerCube textureSource;
uniform int face;
uniform int sampleCount;
uniform float sourceLevel;

out vec4 outputColor;

void main() {
    vec3 N = getCubeDirection(face, vertexTextureCoordinates * 2.0 - 1.0);

    vec3 irradiance = vec3(0.0);
    for (int i = 0; i < sampleCount; i++)
        irradiance += textureLod(textureSource, sampleHemisphereCosine(hammersley(uint(i), uint(sampleCount)), N), sourceLevel).rgb;

    outputColor = vec4(irradiance / float(sampleCount), 1.0);
}
