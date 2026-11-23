#version 330 core

#include "common/ibl.glsl"

/*
 * Draws a face of a mip level of the prefiltered map: the sky blurred as a surface of the roughness of the level
 * reflects it. The samples follow the GGX distribution; every one reads the sky from the mip level that matches the
 * solid angle it represents, which hides the noise of few samples (Colbert and Krivanek, "GPU-Based Importance Sampling").
 * A sky of one color gives that color on every level.
 */
in vec2 vertexTextureCoordinates;

uniform samplerCube textureSource;
uniform int face;
uniform int sampleCount;
uniform float roughness;
uniform float sourceResolution;

out vec4 outputColor;

void main() {
    vec3 N = getCubeDirection(face, vertexTextureCoordinates * 2.0 - 1.0);

    // A mirror shows the sky as it is
    if (roughness < 0.001) {
        outputColor = vec4(textureLod(textureSource, N, 0.0).rgb, 1.0);
        return;
    }

    // The reflection is not seen from the normal only, but this is the usual simplification: V = R = N
    vec3 V = N;
    float texelSolidAngle = 4.0 * PI / (6.0 * sourceResolution * sourceResolution);

    vec3 color = vec3(0.0);
    float weight = 0.0;
    for (int i = 0; i < sampleCount; i++) {
        vec3 H = sampleHalfVectorGGX(hammersley(uint(i), uint(sampleCount)), N, roughness);
        vec3 L = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = dot(N, L);
        if (NdotL > 0.0) {
            float NdotH = max(dot(N, H), 0.0);
            float VdotH = max(dot(V, H), 0.0);

            float pdf = distributionGGX(NdotH, roughness) * NdotH / (4.0 * VdotH) + 0.0001;
            float sampleSolidAngle = 1.0 / (float(sampleCount) * pdf + 0.0001);
            float level = max(0.5 * log2(sampleSolidAngle / texelSolidAngle), 0.0);

            color += textureLod(textureSource, L, level).rgb * NdotL;
            weight += NdotL;
        }
    }

    outputColor = vec4(color / max(weight, 0.0001), 1.0);
}
