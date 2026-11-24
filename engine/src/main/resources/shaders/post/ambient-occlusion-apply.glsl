#version 330 core

#include "common/ambient-occlusion.glsl"

/*
 * Brings the occlusion of half the size to the size of the scene and multiplies the scene by it (by the blending): the
 * four samples around a pixel are weighted by how alike their distances from the eye are, so the shade of an object
 * doesn't leak over its edge onto what is behind it.
 */

/* The blurred occlusion (red) and distances (green) of half the size */
uniform sampler2D textureAo;

/* How much of the occlusion is applied: 0 is none, 1 is all of it */
uniform float aoIntensity;

/* Output color */
out vec4 outputColor;

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    ivec2 size = textureSize(textureDepth, 0);

    // The sky is not darkened
    if (texelFetch(textureDepth, pixel, 0).r >= 1.0) {
        outputColor = vec4(1.0);
        return;
    }

    float eyeDistance = length(getAoPosition(pixel) - cameraPosition);

    // The centers of the texels of the smaller image are at the whole numbers of this position
    vec2 position = (vec2(pixel) + 0.5) * 0.5 - 0.5;
    ivec2 base = ivec2(floor(position));
    vec2 fraction = position - vec2(base);
    ivec2 last = textureSize(textureAo, 0) - 1;

    float sum = 0.0;
    float total = 0.0;
    for (int y = 0; y < 2; y++)
        for (int x = 0; x < 2; x++) {
            vec2 sampled = texelFetch(textureAo, clamp(base + ivec2(x, y), ivec2(0), last), 0).rg;
            float weight = (x == 0 ? 1.0 - fraction.x : fraction.x) * (y == 0 ? 1.0 - fraction.y : fraction.y)
                    * getAoWeight(eyeDistance, sampled.g);

            sum += sampled.r * weight;
            total += weight;
        }

    outputColor = vec4(vec3(mix(1.0, sum / max(total, 1e-6), aoIntensity)), 1.0);
}
