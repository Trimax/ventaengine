#version 330 core

#include "common/camera.glsl"
#include "common/environment.glsl"
#include "common/flags.glsl"
#include "common/fog.glsl"
#include "common/lights.glsl"
#include "common/noise.glsl"
#include "common/pbr.glsl"
#include "common/point-shadow.glsl"
#include "common/shadow.glsl"

/***
 * Definitions
 ***/

const int MAX_MATERIALS = 8;

/***
 * Structures
 ***/

struct Material {
    vec4 color;
    vec2 tiling;
    vec2 offset;
    float metalness;
    float roughness;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec3 vertexNormal;
in vec3 vertexTangent;
in vec3 vertexBitangent;
in vec3 vertexPosition;
in float vertexHeight;
in vec2 vertexTextureCoordinates;
in float vertexTimeElapsed;

/* Textures */
uniform sampler2DArray textureDiffuse;
uniform sampler2DArray textureNormal;
uniform sampler2DArray textureRoughness;
uniform sampler2DArray textureMetalness;
uniform sampler2DArray textureAmbientOcclusion;

/* Feature flags */
uniform int useTextureNormal;
uniform int useTextureRoughness;

/* Materials */
uniform Material materials[MAX_MATERIALS];
uniform int materialCount;

/* Elevations & height */
uniform float elevations[MAX_MATERIALS];
uniform float factor;

/* Time */
uniform float timeElapsed;

/* Output color */
out vec4 outputColor;

/***
 * Materials
 ***/

vec2 getTextureCoordinates(int layer) {
    return vertexTextureCoordinates * materials[layer].tiling + materials[layer].offset;
}

/***
 * Texture repetition
 ***/

/*
 * A tiled texture without visible repetition (I. Quilez, "texture repetition", technique 3): a slow noise picks
 * one of 8 random offsets of the texture and neighbour offsets are blended. Explicit gradients keep the mip level
 * of the original coordinates, so there are no seams where the offset changes.
 */
vec4 sampleWithoutRepetition(sampler2DArray textures, vec2 coordinates, float layer) {
    float index = noise(coordinates * 0.35) * 8.0;
    float lower = floor(index);
    float fraction = fract(index);

    vec2 offset0 = sin(vec2(3.0, 7.0) * lower);
    vec2 offset1 = sin(vec2(3.0, 7.0) * (lower + 1.0));

    vec2 dx = dFdx(coordinates);
    vec2 dy = dFdy(coordinates);
    vec4 value0 = textureGrad(textures, vec3(coordinates + offset0, layer), dx, dy);
    vec4 value1 = textureGrad(textures, vec3(coordinates + offset1, layer), dx, dy);

    return mix(value0, value1, smoothstep(0.2, 0.8, fraction));
}

vec4 sampleLayers(sampler2DArray textures, int layer0, int layer1, float blend) {
    vec4 value0 = sampleWithoutRepetition(textures, getTextureCoordinates(layer0), float(layer0));
    vec4 value1 = sampleWithoutRepetition(textures, getTextureCoordinates(layer1), float(layer1));
    return mix(value0, value1, blend);
}

/* Large patches of slightly brighter and darker ground, so wide areas of one material don't look uniform */
float getBrightnessVariation() {
    vec2 point = vertexTextureCoordinates * 6.0;
    float variation = noise(point) * 0.6 + noise(point * 2.7 + 13.0) * 0.4;
    return mix(0.82, 1.12, variation);
}

vec3 getNormal(int layer0, int layer1, float blend) {
    vec3 normal = normalize(vertexNormal);
    if (!isSet(useTextureNormal))
        return normal;

    // Rows of the texture go along +v, while the green channel of an OpenGL normal map points up the image (-v)
    vec3 mapped = sampleLayers(textureNormal, layer0, layer1, blend).rgb * 2.0 - 1.0;
    mat3 tbn = mat3(normalize(vertexTangent), -normalize(vertexBitangent), normal);
    return normalize(tbn * mapped);
}

float getRoughness(int layer0, int layer1, float blend) {
    if (isSet(useTextureRoughness))
        return sampleLayers(textureRoughness, layer0, layer1, blend).r;

    return mix(materials[layer0].roughness, materials[layer1].roughness, blend);
}

/***
 * Lighting functions
 ***/

/* The terrain is not a metal; its roughness comes from its materials */
const float TERRAIN_METALNESS = 0.0;

vec3 calculateLight(Light light, vec3 baseColor, vec3 normal, vec3 cameraDirection, float roughness) {
    if (light.enabled == 0)
        return vec3(0.0);

    vec3 toLight = light.position - vertexPosition;
    float distance = length(toLight);
    vec3 radiance = light.color * light.intensity * getLightFactor(light, distance) * getPointShadow(light, vertexPosition, normalize(vertexNormal));

    return pbrLight(normal, cameraDirection, toLight / max(distance, 0.0001), baseColor, TERRAIN_METALNESS, roughness, radiance);
}

vec3 calculateDirectionalLight(vec3 baseColor, vec3 normal, vec3 cameraDirection, float roughness) {
    if (!isSet(useDirectionalLight))
        return vec3(0.0);

    vec3 radiance = directionalLight.color * directionalLight.intensity * getSunShadow(vertexPosition, normalize(vertexNormal));

    return pbrLight(normal, cameraDirection, normalize(-directionalLight.direction), baseColor, TERRAIN_METALNESS, roughness, radiance);
}

/* The light of the environment, the sun and the lights; the result is HDR, the post processing tone maps it */
vec3 computeLighting(vec3 baseColor, vec3 normal, vec3 cameraDirection, float roughness) {
    vec3 result = getAmbientLight(normal, cameraDirection, baseColor, TERRAIN_METALNESS, roughness, 1.0)
            + calculateDirectionalLight(baseColor, normal, cameraDirection, roughness);

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], baseColor, normal, cameraDirection, roughness);

    return result;
}

vec3 applyFog(vec3 color) {
    if (!isSet(useFog))
        return color;

    return mix(color, getFogColor(cameraPosition, vertexPosition), getFogAmount(cameraPosition, vertexPosition));
}

/*
 * Material i covers the heights from elevations[i - 1] to elevations[i].
 * Near a boundary the two neighbour materials are blended symmetrically, so both sides meet at 50/50 without a seam.
 */
void getMaterialLayers(float y, float blendWidth, out int layer0, out int layer1, out float blend) {
    int current = materialCount - 1;
    for (int i = 0; i < materialCount; i++)
        if (y < elevations[i]) {
            current = i;
            break;
        }

    float halfWidth = blendWidth * 0.5;
    if (current > 0 && y < elevations[current - 1] + halfWidth) {
        layer0 = current - 1;
        layer1 = current;
        blend = smoothstep(elevations[current - 1] - halfWidth, elevations[current - 1] + halfWidth, y);
        return;
    }

    if (current < materialCount - 1 && y > elevations[current] - halfWidth) {
        layer0 = current;
        layer1 = current + 1;
        blend = smoothstep(elevations[current] - halfWidth, elevations[current] + halfWidth, y);
        return;
    }

    layer0 = current;
    layer1 = current;
    blend = 0.0;
}

void main() {
    // A flat terrain (factor 0) has no height range
    float range = max(factor, 0.0001);
    float yNormalized = clamp(vertexHeight / range + 0.5, 0.0, 1.0);

    int layer0;
    int layer1;
    float blend;
    getMaterialLayers(yNormalized, 0.05, layer0, layer1, blend);

    vec4 materialColor = mix(materials[layer0].color, materials[layer1].color, blend);
    vec3 baseColor = sampleLayers(textureDiffuse, layer0, layer1, blend).rgb * materialColor.rgb * getBrightnessVariation();
    vec3 normal = getNormal(layer0, layer1, blend);
    float roughness = getRoughness(layer0, layer1, blend);
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);

    outputColor = vec4(applyFog(computeLighting(baseColor, normal, cameraDirection, roughness)), 1.0);
}
