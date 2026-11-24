#include "common/camera.glsl"
#include "common/flags.glsl"
#include "common/lights.glsl"

const int MAX_CASCADES = 4;

/* How far a point is moved before it is compared with the shadow map, in texels of the cascade */
const float SHADOW_NORMAL_OFFSET = 1.5;
const float SHADOW_LIGHT_OFFSET = 1.5;

/* The width of the filter of the soft edge of a shadow, in texels */
const int SHADOW_FILTER_RADIUS = 1;

/*
 * Shadows of the sun: a layer of the depth texture array for every cascade. The sampler compares the depth of a point
 * with the stored one and filters the results, so it gives how much of the point is lit.
 */
uniform sampler2DArrayShadow textureShadow;
uniform int useShadows;
uniform int shadowCascadeCount;
uniform mat4 shadowMatrices[MAX_CASCADES];
uniform float shadowSplits[MAX_CASCADES];
uniform float shadowTexels[MAX_CASCADES];
uniform float shadowDistance;

/* The cascade of a point by its distance from the camera along the view direction, the count when there is none */
int getShadowCascade(float depth) {
    for (int i = 0; i < shadowCascadeCount; i++)
        if (depth < shadowSplits[i])
            return i;

    return shadowCascadeCount;
}

/*
 * How much of the light of the sun reaches a point of the air: 1 is all of it, 0 is none. The air has no surface, so
 * there is no normal to move the point along: it is only moved towards the sun, which is enough for the points near the ground.
 */
float getSunVisibility(vec3 position) {
    if (!isSet(useShadows))
        return 1.0;

    float depth = dot(position - cameraPosition, cameraForward);
    int cascade = getShadowCascade(depth);
    if (cascade >= shadowCascadeCount)
        return 1.0;

    vec3 shifted = position + normalize(-directionalLight.direction) * (shadowTexels[cascade] * SHADOW_LIGHT_OFFSET);
    vec3 coordinates = (shadowMatrices[cascade] * vec4(shifted, 1.0)).xyz * 0.5 + 0.5;
    if (coordinates.z > 1.0)
        return 1.0;

    // One comparison: the hardware filters it, and a march takes many points, so their sum is smooth
    float lit = texture(textureShadow, vec4(coordinates.xy, float(cascade), coordinates.z));
    return mix(lit, 1.0, smoothstep(0.85 * shadowDistance, shadowDistance, depth));
}

/*
 * How much of the light of the sun reaches a point: 1 is all of it, 0 is none (the point is in a shadow).
 * The normal is the geometric normal of the surface in the world space.
 */
float getSunShadow(vec3 position, vec3 normal) {
    if (!isSet(useShadows))
        return 1.0;

    float depth = dot(position - cameraPosition, cameraForward);
    int cascade = getShadowCascade(depth);
    if (cascade >= shadowCascadeCount)
        return 1.0;

    // Without the offsets a surface shadows itself (acne): the farther the cascade, the coarser its texels and the offsets
    vec3 toLight = normalize(-directionalLight.direction);
    float cosine = clamp(dot(normal, toLight), 0.0, 1.0);
    float sine = sqrt(1.0 - cosine * cosine);
    float texel = shadowTexels[cascade];
    vec3 shifted = position + normal * (texel * SHADOW_NORMAL_OFFSET * sine) + toLight * (texel * SHADOW_LIGHT_OFFSET);

    vec3 coordinates = (shadowMatrices[cascade] * vec4(shifted, 1.0)).xyz * 0.5 + 0.5;
    if (coordinates.z > 1.0)
        return 1.0;

    vec2 step = 1.0 / vec2(textureSize(textureShadow, 0).xy);
    float lit = 0.0;
    for (int x = -SHADOW_FILTER_RADIUS; x <= SHADOW_FILTER_RADIUS; x++)
        for (int y = -SHADOW_FILTER_RADIUS; y <= SHADOW_FILTER_RADIUS; y++)
            lit += texture(textureShadow, vec4(coordinates.xy + vec2(x, y) * step, float(cascade), coordinates.z));

    lit /= float((2 * SHADOW_FILTER_RADIUS + 1) * (2 * SHADOW_FILTER_RADIUS + 1));

    // The shadows fade out at the end of the last cascade instead of ending with an edge
    return mix(lit, 1.0, smoothstep(0.85 * shadowDistance, shadowDistance, depth));
}
