#include "common/flags.glsl"
#include "common/lights.glsl"
#include "common/pbr.glsl"

/*
 * The light of the environment (image based lighting). The skybox is convolved on start into
 *  - the irradiance map: the light arriving from all around a direction, it is what a surface diffuses,
 *  - the prefiltered map: the sky blurred by the roughness (the mip level is the roughness), it is what a surface reflects,
 *  - the lookup table of the BRDF: the scale and the bias of the fresnel, by the angle and the roughness.
 * The ambient light of the scene multiplies it (white keeps the sky as it is, a darker color dims it), and a scene
 * without a skybox is lit by the ambient light alone, as a flat color.
 */
uniform samplerCube textureIrradiance;
uniform samplerCube texturePrefilter;
uniform sampler2D textureBrdf;
uniform int useEnvironment;
uniform float prefilterLevels;

/*
 * An approximation of the lookup table (Karis, "Physically Based Shading on Mobile") for a scene without a skybox.
 * Returns the scale and the bias of the fresnel.
 */
vec2 getEnvironmentBrdfApproximation(float NdotV, float roughness) {
    const vec4 c0 = vec4(-1.0, -0.0275, -0.572, 0.022);
    const vec4 c1 = vec4(1.0, 0.0425, 1.04, -0.04);

    vec4 r = roughness * c0 + c1;
    float a004 = min(r.x * r.x, exp2(-9.28 * NdotV)) * r.x + r.y;

    return vec2(-1.04, 1.04) * a004 + r.zw;
}

/*
 * The light from the environment (or the ambient light) that a surface diffuses and reflects towards the viewer.
 * The occlusion (the ambient occlusion map) darkens it.
 */
vec3 getAmbientLight(vec3 N, vec3 V, vec3 albedo, float metalness, float roughness, float occlusion) {
    float r = getPbrRoughness(roughness);
    float NdotV = max(dot(N, V), 0.0001);

    vec3 F = fresnelSchlickRoughness(NdotV, getReflectance(albedo, metalness), r);
    vec3 diffuse = (1.0 - F) * (1.0 - metalness) * albedo;

    vec3 irradiance = ambientLight;
    vec3 prefiltered = ambientLight;
    vec2 brdf = getEnvironmentBrdfApproximation(NdotV, r);

    if (isSet(useEnvironment)) {
        irradiance *= texture(textureIrradiance, N).rgb;
        prefiltered *= textureLod(texturePrefilter, reflect(-V, N), r * (prefilterLevels - 1.0)).rgb;
        brdf = texture(textureBrdf, vec2(NdotV, r)).rg;
    }

    return (diffuse * irradiance + prefiltered * (F * brdf.x + brdf.y)) * occlusion;
}
