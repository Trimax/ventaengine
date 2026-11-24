#version 330 core

#include "common/ambient-occlusion.glsl"

/*
 * Computes the occlusion for an image of half the size of the scene: a pixel of it stands for a pixel of the scene. The
 * red is the occlusion, the green is the distance of the pixel from the eye (the next passes weigh the samples by it).
 */

/* Output color */
out vec4 outputColor;

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy) * 2;
    ivec2 size = textureSize(textureDepth, 0);

    // The sky has nothing that occludes it
    if (texelFetch(textureDepth, min(pixel, size - 1), 0).r >= 1.0) {
        outputColor = vec4(1.0, 1e6, 0.0, 1.0);
        return;
    }

    float eyeDistance;
    float occlusion = getAmbientOcclusion(min(pixel, size - 1), lodDither(gl_FragCoord.xy), eyeDistance);

    outputColor = vec4(occlusion, eyeDistance, 0.0, 1.0);
}
