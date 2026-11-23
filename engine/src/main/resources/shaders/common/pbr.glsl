/*
 * Physically based shading: the Cook-Torrance BRDF with the GGX distribution, the Smith geometry term and the Schlick
 * fresnel, driven by the albedo, the metalness and the roughness of a surface. The colors are linear.
 */

const float PI = 3.14159265359;

/* A smoother surface would have a highlight of the size of a point, which can't be drawn (and flickers) */
const float ROUGHNESS_MINIMAL = 0.045;

/* How much light a non-metal reflects when it is seen straight on */
const float DIELECTRIC_REFLECTANCE = 0.04;

float getPbrRoughness(float roughness) {
    return clamp(roughness, ROUGHNESS_MINIMAL, 1.0);
}

/* The color of the reflection at the normal incidence: a non-metal reflects white, a metal reflects its own color */
vec3 getReflectance(vec3 albedo, float metalness) {
    return mix(vec3(DIELECTRIC_REFLECTANCE), albedo, metalness);
}

/* GGX (Trowbridge-Reitz): how many microfacets of the surface face the half vector */
float distributionGGX(float NdotH, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float d = NdotH * NdotH * (a2 - 1.0) + 1.0;

    return a2 / (PI * d * d);
}

float geometrySchlickGGX(float NdotX, float k) {
    return NdotX / (NdotX * (1.0 - k) + k);
}

/* Smith: how many microfacets are shadowed or masked by their neighbors, k is for the direct light */
float geometrySmith(float NdotV, float NdotL, float roughness) {
    float r = roughness + 1.0;
    float k = r * r / 8.0;

    return geometrySchlickGGX(NdotV, k) * geometrySchlickGGX(NdotL, k);
}

/* Schlick: a surface reflects more at grazing angles */
vec3 fresnelSchlick(float cosine, vec3 reflectance) {
    return reflectance + (1.0 - reflectance) * pow(clamp(1.0 - cosine, 0.0, 1.0), 5.0);
}

/* The fresnel of the light from the environment: a rough surface doesn't reflect that much at grazing angles */
vec3 fresnelSchlickRoughness(float cosine, vec3 reflectance, float roughness) {
    return reflectance + (max(vec3(1.0 - roughness), reflectance) - reflectance) * pow(clamp(1.0 - cosine, 0.0, 1.0), 5.0);
}

/*
 * The light reflected towards the viewer from one light source.
 * N is the normal, V the direction to the viewer, L the direction to the light, the radiance is the color of the light
 * multiplied by its intensity (and its attenuation).
 *
 * The intensity of a light is an artistic value: a white light of the intensity 1 lights a white surface facing it
 * with the color 1 (the diffuse part isn't divided by pi), so the specular part is multiplied by pi to keep their ratio.
 */
vec3 pbrLight(vec3 N, vec3 V, vec3 L, vec3 albedo, float metalness, float roughness, vec3 radiance) {
    float NdotL = dot(N, L);
    if (NdotL <= 0.0)
        return vec3(0.0);

    float r = getPbrRoughness(roughness);
    vec3 H = normalize(V + L);
    float NdotV = max(dot(N, V), 0.0001);
    float NdotH = max(dot(N, H), 0.0);

    vec3 F = fresnelSchlick(max(dot(H, V), 0.0), getReflectance(albedo, metalness));
    float D = distributionGGX(NdotH, r);
    float G = geometrySmith(NdotV, NdotL, r);

    vec3 specular = D * G * F / max(4.0 * NdotV * NdotL, 0.001);

    // What is reflected isn't diffused, and metals don't diffuse at all
    vec3 diffuse = (1.0 - F) * (1.0 - metalness) * albedo;

    return (diffuse + PI * specular) * radiance * NdotL;
}
