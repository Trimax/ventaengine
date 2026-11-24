#version 330 core

#include "common/bloom.glsl"

/*
 * Halves the image for the bloom: 13 taps of the source around the pixel make a smooth reduction (the weights of the
 * five boxes of four taps are 0.5 for the middle one and 0.125 for the corners). The first pass also takes only what
 * glows and averages the boxes with the weight that tames the sparks, the others are plain reductions.
 */

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* The image to reduce and the size of one of its texels */
uniform sampler2D textureScene;
uniform vec2 texelSize;

/* The first pass of the chain: it picks the bright part */
uniform int bloomFirst;
uniform float bloomThreshold;
uniform float bloomKnee;

/* Output color */
out vec4 outputColor;

vec3 fetch(vec2 offset) {
    vec3 color = texture(textureScene, vertexTextureCoordinates + offset * texelSize).rgb;

    return bloomFirst != 0 ? bloomPrefilter(color, bloomThreshold, bloomKnee) : color;
}

vec3 box(vec3 a, vec3 b, vec3 c, vec3 d) {
    return (a + b + c + d) * 0.25;
}

void main() {
    vec3 a = fetch(vec2(-2.0, -2.0));
    vec3 b = fetch(vec2(0.0, -2.0));
    vec3 c = fetch(vec2(2.0, -2.0));
    vec3 d = fetch(vec2(-2.0, 0.0));
    vec3 e = fetch(vec2(0.0, 0.0));
    vec3 f = fetch(vec2(2.0, 0.0));
    vec3 g = fetch(vec2(-2.0, 2.0));
    vec3 h = fetch(vec2(0.0, 2.0));
    vec3 i = fetch(vec2(2.0, 2.0));
    vec3 j = fetch(vec2(-1.0, -1.0));
    vec3 k = fetch(vec2(1.0, -1.0));
    vec3 l = fetch(vec2(-1.0, 1.0));
    vec3 m = fetch(vec2(1.0, 1.0));

    vec3 middle = box(j, k, l, m);
    vec3 corners[4] = vec3[4](box(a, b, d, e), box(b, c, e, f), box(d, e, g, h), box(e, f, h, i));

    if (bloomFirst == 0) {
        vec3 result = middle * 0.5 + (corners[0] + corners[1] + corners[2] + corners[3]) * 0.125;
        outputColor = vec4(result, 1.0);
        return;
    }

    // The boxes are averaged by the weight of their brightness, so a lone spark doesn't flicker in the blur
    float sum = 0.0;
    vec3 result = vec3(0.0);
    for (int index = 0; index < 4; index++) {
        float weight = 0.125 * bloomKarisWeight(corners[index]);
        result += corners[index] * weight;
        sum += weight;
    }

    float weight = 0.5 * bloomKarisWeight(middle);
    result += middle * weight;
    sum += weight;

    outputColor = vec4(result / sum, 1.0);
}
