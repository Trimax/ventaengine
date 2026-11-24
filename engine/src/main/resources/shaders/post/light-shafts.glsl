#version 330 core

#include "common/light-shafts.glsl"

/*
 * Marches the view rays of an image of half the size of the scene and writes the light of the sun that is scattered in the
 * fog along them. The alpha is the length of the ray, so the next pass can tell which pixels of the scene a sample suits.
 */

/* Output color */
out vec4 outputColor;

void main() {
    // The pixel of the scene that this one stands for
    ivec2 size = textureSize(textureDepth, 0);
    ivec2 pixel = min(ivec2(gl_FragCoord.xy) * 2, size - 1);

    vec3 direction;
    float rayLength = getShaftsLength((vec2(pixel) + 0.5) / vec2(size), texelFetch(textureDepth, pixel, 0).r, direction);

    outputColor = vec4(getLightShafts(direction, rayLength, lodDither(gl_FragCoord.xy)), rayLength);
}
