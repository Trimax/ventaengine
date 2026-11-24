#version 330 core

#include "common/light-shafts.glsl"

/*
 * Brings the shafts of half the size to the size of the scene and adds them to it (by the blending). The four samples
 * around a pixel are weighted by how alike their rays are to the ray of the pixel, so the shafts of the sky don't
 * leak over the edge of a castle that stands against it.
 */

/* The shafts of half the size: the light in the colors and the length of the ray in the alpha */
uniform sampler2D textureShafts;

/* How bright the shafts are */
uniform float lightShaftsIntensity;

/* Output color */
out vec4 outputColor;

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    ivec2 size = textureSize(textureDepth, 0);

    vec3 direction;
    float rayLength = getShaftsLength((vec2(pixel) + 0.5) / vec2(size), texelFetch(textureDepth, pixel, 0).r, direction);

    // The centers of the texels of the smaller image are at the whole numbers of this position
    vec2 position = (vec2(pixel) + 0.5) * 0.5 - 0.5;
    ivec2 base = ivec2(floor(position));
    vec2 fraction = position - vec2(base);
    ivec2 last = textureSize(textureShafts, 0) - 1;

    vec3 sum = vec3(0.0);
    float total = 0.0;
    for (int y = 0; y < 2; y++)
        for (int x = 0; x < 2; x++) {
            vec4 shafts = texelFetch(textureShafts, clamp(base + ivec2(x, y), ivec2(0), last), 0);
            float weight = (x == 0 ? 1.0 - fraction.x : fraction.x) * (y == 0 ? 1.0 - fraction.y : fraction.y)
                    * getShaftsWeight(rayLength, shafts.a);

            sum += shafts.rgb * weight;
            total += weight;
        }

    outputColor = vec4(sum / max(total, 1e-6) * lightShaftsIntensity, 0.0);
}
