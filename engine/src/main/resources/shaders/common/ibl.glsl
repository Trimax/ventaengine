#include "common/pbr.glsl"

/*
 * The convolution of the skybox for the image based lighting: directions of the texels of a cube map, the low
 * discrepancy sampling of a hemisphere and the integral of the BRDF. It runs once when a skybox is set.
 */

/*
 * The direction of a texel of a face of a cube map. The texel is given by its position (a, b) across the face, both from
 * -1 to 1, b grows with the rows of the image. These are the rules of OpenGL for the faces in the order
 * +X, -X, +Y, -Y, +Z, -Z: the same direction looks up the same texel.
 */
vec3 getCubeDirection(int face, vec2 position) {
    float a = position.x;
    float b = position.y;

    if (face == 0)
        return normalize(vec3(1.0, -b, -a));

    if (face == 1)
        return normalize(vec3(-1.0, -b, a));

    if (face == 2)
        return normalize(vec3(a, 1.0, b));

    if (face == 3)
        return normalize(vec3(a, -1.0, -b));

    if (face == 4)
        return normalize(vec3(a, -b, 1.0));

    return normalize(vec3(-a, -b, -1.0));
}

/* The Van der Corput sequence: the bits of a number mirrored around the point, a number from 0 to 1 */
float radicalInverse(uint bits) {
    bits = (bits << 16u) | (bits >> 16u);
    bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
    bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
    bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
    bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);

    return float(bits) * 2.3283064365386963e-10;
}

/* The i-th of count points evenly spread over the unit square */
vec2 hammersley(uint i, uint count) {
    return vec2(float(i) / float(count), radicalInverse(i));
}

/* A point on the hemisphere around a normal, more of them near the normal: the cosine of the angle is the weight */
vec3 sampleHemisphereCosine(vec2 point, vec3 N) {
    float phi = 2.0 * PI * point.x;
    float cosine = sqrt(1.0 - point.y);
    float sine = sqrt(point.y);

    vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
    vec3 tangent = normalize(cross(up, N));
    vec3 bitangent = cross(N, tangent);

    return normalize(tangent * (cos(phi) * sine) + bitangent * (sin(phi) * sine) + N * cosine);
}

/* A half vector around a normal distributed as the GGX distribution of the roughness */
vec3 sampleHalfVectorGGX(vec2 point, vec3 N, float roughness) {
    float a = roughness * roughness;
    float phi = 2.0 * PI * point.x;
    float cosine = sqrt((1.0 - point.y) / (1.0 + (a * a - 1.0) * point.y));
    float sine = sqrt(1.0 - cosine * cosine);

    vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
    vec3 tangent = normalize(cross(up, N));
    vec3 bitangent = cross(N, tangent);

    return normalize(tangent * (cos(phi) * sine) + bitangent * (sin(phi) * sine) + N * cosine);
}

/*
 * The scale and the bias of the fresnel: the reflected part of the light from all directions for a viewer at the
 * angle with the cosine NdotV to a surface of the roughness. The reflection is F0 * scale + bias.
 */
vec2 integrateBrdf(float NdotV, float roughness, int sampleCount) {
    vec3 V = vec3(sqrt(1.0 - NdotV * NdotV), 0.0, NdotV);
    vec3 N = vec3(0.0, 0.0, 1.0);

    // The geometry term of the light from the environment
    float k = roughness * roughness / 2.0;

    float scale = 0.0;
    float bias = 0.0;
    for (int i = 0; i < sampleCount; i++) {
        vec3 H = sampleHalfVectorGGX(hammersley(uint(i), uint(sampleCount)), N, roughness);
        vec3 L = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(L.z, 0.0);
        float NdotH = max(H.z, 0.0);
        float VdotH = max(dot(V, H), 0.0);

        if (NdotL > 0.0) {
            float G = geometrySchlickGGX(NdotV, k) * geometrySchlickGGX(NdotL, k);
            float visibility = G * VdotH / max(NdotH * NdotV, 0.0001);
            float fresnel = pow(1.0 - VdotH, 5.0);

            scale += (1.0 - fresnel) * visibility;
            bias += fresnel * visibility;
        }
    }

    return vec2(scale, bias) / float(sampleCount);
}
