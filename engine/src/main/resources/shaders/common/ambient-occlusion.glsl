/*
 * The ambient occlusion of a screen: how much of the light of the sky is kept from a point by what stands near it. It is
 * estimated from the depth of the scene alone: the world positions of the pixels around a pixel are looked at, and the
 * ones that stand high above the surface of the pixel, near it, darken it (the form of Alchemy and of the scalable
 * ambient obscurance: the occlusion of a sample is how far it rises over the surface compared to its distance).
 */

#include "common/camera.glsl"
#include "common/lod.glsl"

/* The depth of the scene and the inverse of its view and projection matrices */
uniform sampler2D textureDepth;
uniform mat4 matrixViewProjectionInverse;

/* How far around a point the objects darken it (the units of the world), how sharply the occlusion grows, and how many pixels a unit of the world is at the distance of 1 */
uniform float aoRadius;
uniform float aoPower;
uniform float aoProjectionScale;

/* The samples of a point, they go along a spiral and are turned by the noise of the pixel */
const int AO_SAMPLES = 12;

/* The golden angle: a spiral of it spreads the samples evenly */
const float AO_GOLDEN_ANGLE = 2.399963;

/* How far (in the pixels) the samples go: not less than a couple of pixels, or nothing is seen, and not too far, or the cost grows */
const float AO_MINIMAL_PIXELS = 2.0;
const float AO_MAXIMAL_PIXELS = 48.0;

/* A sample must rise over the surface by this share of the radius, or the depth that is not exact makes a surface darken itself */
const float AO_BIAS = 0.03;

/* The point of the world a pixel of the scene sees: the pixel is a texel of the depth */
vec3 getAoPosition(ivec2 pixel) {
    ivec2 size = textureSize(textureDepth, 0);
    ivec2 clamped = clamp(pixel, ivec2(0), size - 1);
    float depth = texelFetch(textureDepth, clamped, 0).r;

    vec4 position = matrixViewProjectionInverse * vec4(vec3((vec2(clamped) + 0.5) / vec2(size), depth) * 2.0 - 1.0, 1.0);
    return position.xyz / position.w;
}

/*
 * The normal of the surface a point lies on, from the points around it: the neighbor that is nearer to the point in the
 * depth is taken on every axis, so the normal at the edge of an object is the normal of the object and not of the
 * jump between it and what is behind. It faces the eye.
 */
vec3 getAoNormal(vec3 center, vec3 right, vec3 left, vec3 up, vec3 down, vec3 eye) {
    float distance = length(center - eye);
    vec3 horizontal = abs(length(right - eye) - distance) < abs(length(left - eye) - distance) ? right - center : center - left;
    vec3 vertical = abs(length(up - eye) - distance) < abs(length(down - eye) - distance) ? up - center : center - down;

    return normalize(cross(horizontal, vertical));
}

/*
 * How much a sample darkens a point, from 0 to 1: the more it rises over the surface of the point and the nearer it is,
 * the more. A sample at or below the surface darkens nothing, and neither does one beyond the radius.
 */
float getAoSampleOcclusion(vec3 center, vec3 normal, vec3 sampled, float radius) {
    vec3 way = sampled - center;
    float square = dot(way, way);
    float radiusSquare = radius * radius;
    if (square >= radiusSquare)
        return 0.0;

    float rise = dot(way, normal) - AO_BIAS * radius;
    float occlusion = clamp(radius * rise / (square + 0.01 * radiusSquare), 0.0, 1.0);

    // A sample at the edge of the radius counts less: an object that comes into it doesn't make the point flash
    return occlusion * (1.0 - square / radiusSquare);
}

/* How much light a point keeps from 0 (all of it is kept from it) to 1 (nothing darkens it), the pixel is a texel of the depth */
float getAmbientOcclusion(ivec2 pixel, float noise, out float eyeDistance) {
    vec3 center = getAoPosition(pixel);
    vec3 normal = getAoNormal(center, getAoPosition(pixel + ivec2(1, 0)), getAoPosition(pixel - ivec2(1, 0)),
            getAoPosition(pixel + ivec2(0, 1)), getAoPosition(pixel - ivec2(0, 1)), cameraPosition);

    eyeDistance = length(center - cameraPosition);
    float pixels = clamp(aoRadius * aoProjectionScale / max(eyeDistance, 1e-3), AO_MINIMAL_PIXELS, AO_MAXIMAL_PIXELS);

    float sum = 0.0;
    for (int i = 0; i < AO_SAMPLES; i++) {
        float angle = (float(i) + noise) * AO_GOLDEN_ANGLE;
        float fraction = sqrt((float(i) + 0.5) / float(AO_SAMPLES));

        ivec2 offset = ivec2(round(vec2(cos(angle), sin(angle)) * fraction * pixels));
        sum += getAoSampleOcclusion(center, normal, getAoPosition(pixel + offset), aoRadius);
    }

    // The factor of 2 is the one of the formula of Alchemy: the samples cover a hemisphere, and the cosine of the surface halves what they give
    return pow(clamp(1.0 - 2.0 * sum / float(AO_SAMPLES), 0.0, 1.0), aoPower);
}

/* How much a sample of the occlusion counts for a pixel by how alike their distances from the eye are */
float getAoWeight(float eyeDistance, float sampled) {
    return 1.0 / (abs(eyeDistance - sampled) / max(max(eyeDistance, sampled), 1.0) + 0.02);
}
