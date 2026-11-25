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
#include "common/terrain-blend.glsl"

/***
 * Definitions
 ***/

const int MAX_MATERIALS = 8;
const int MAX_SLOPES = 2;

/* The detail normal: the normal map of the material again, smaller, near the camera */
const float DETAIL_TILING = 8.0;
const float DETAIL_NEAR = 15.0;
const float DETAIL_FAR = 60.0;
const float DETAIL_STRENGTH = 0.8;

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

/* Materials: the ones of the heights, then the ones of the steepness of the ground */
uniform Material materials[MAX_MATERIALS];
uniform int materialCount;
uniform int heightMaterialCount;

/* The materials of the steepness: the angles (in degrees) between which they cover the ground, and how softly */
uniform int slopeCount;
uniform float slopeMinimums[MAX_SLOPES];
uniform float slopeMaximums[MAX_SLOPES];
uniform float slopeBlends[MAX_SLOPES];

/* The size of the terrain in the world */
uniform vec2 worldSize;

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
vec4 sampleWithoutRepetition(sampler2DArray textures, vec2 coordinates, float layer, vec2 dx, vec2 dy) {
    float index = noise(coordinates * 0.35) * 8.0;
    float lower = floor(index);
    float fraction = fract(index);

    vec2 offset0 = sin(vec2(3.0, 7.0) * lower);
    vec2 offset1 = sin(vec2(3.0, 7.0) * (lower + 1.0));

    vec4 value0 = textureGrad(textures, vec3(coordinates + offset0, layer), dx, dy);
    vec4 value1 = textureGrad(textures, vec3(coordinates + offset1, layer), dx, dy);

    return mix(value0, value1, smoothstep(0.2, 0.8, fraction));
}

vec4 sampleLayers(sampler2DArray textures, int layer0, int layer1, float blend) {
    vec2 coordinates0 = getTextureCoordinates(layer0);
    vec2 coordinates1 = getTextureCoordinates(layer1);
    vec4 value0 = sampleWithoutRepetition(textures, coordinates0, float(layer0), dFdx(coordinates0), dFdy(coordinates0));
    vec4 value1 = sampleWithoutRepetition(textures, coordinates1, float(layer1), dFdx(coordinates1), dFdy(coordinates1));
    return mix(value0, value1, blend);
}

/* Large patches of slightly brighter and darker ground, so wide areas of one material don't look uniform */
float getBrightnessVariation() {
    vec2 point = vertexTextureCoordinates * 6.0;
    float variation = noise(point) * 0.6 + noise(point * 2.7 + 13.0) * 0.4;
    return mix(0.82, 1.12, variation);
}

/* The frame of the normal maps: rows of the texture go along +v, while the green channel of an OpenGL normal map points up the image (-v) */
mat3 getTangentFrame(vec3 normal) {
    return mat3(normalize(vertexTangent), -normalize(vertexBitangent), normal);
}

/*
 * Patches of lighter and darker ground by the position in the world (not by the map: the same on a wall as on a slope),
 * larger than the repeat of the texture and of two sizes, so the pattern of a texture of the steep ground doesn't repeat in rows.
 */
float getMacroVariation01() {
    vec2 point = (vertexPosition.xz + vec2(vertexPosition.y * 0.7, -vertexPosition.y * 0.4)) / max(worldSize, vec2(0.0001)) * 24.0;
    return noise(point) * 0.5 + noise(point * 3.1 + 17.0) * 0.3 + noise(point * 9.7 + 41.0) * 0.2;
}

float getMacroVariation() {
    return mix(0.6, 1.35, getMacroVariation01());
}

vec3 getNormal(int layer0, int layer1, float blend) {
    vec3 normal = normalize(vertexNormal);
    if (!isSet(useTextureNormal))
        return normal;

    vec3 mapped = sampleLayers(textureNormal, layer0, layer1, blend).rgb * 2.0 - 1.0;
    return normalize(getTangentFrame(normal) * mapped);
}

/*
 * Small bumps close to the camera: the normal map of a material at a finer tiling, added to the normal as the change it
 * makes of the normal of the ground. It fades out with the distance, where the bumps would only shimmer.
 */
vec3 applyDetail(vec3 normal, int layer) {
    // The gradients are taken outside of the branch: the sampling in it must not depend on the neighbours
    vec2 coordinates = getTextureCoordinates(layer) * DETAIL_TILING;
    vec2 dx = dFdx(coordinates);
    vec2 dy = dFdy(coordinates);

    float fade = 1.0 - smoothstep(DETAIL_NEAR, DETAIL_FAR, distance(cameraPosition, vertexPosition));
    if (!isSet(useTextureNormal) || fade <= 0.0)
        return normal;

    vec3 ground = normalize(vertexNormal);
    vec3 mapped = textureGrad(textureNormal, vec3(coordinates, float(layer)), dx, dy).rgb * 2.0 - 1.0;
    vec3 detail = getTangentFrame(ground) * mapped - ground;

    return normalize(normal + detail * fade * DETAIL_STRENGTH);
}

/***
 * Triplanar sampling
 ***/

/* The coordinates of the point in the three planes and their gradients, which don't depend on the material */
struct Triplanar {
    vec3 weights;
    vec3 normal;
    vec2 coordinates[3];
    vec2 dx[3];
    vec2 dy[3];
};

Triplanar getTriplanar() {
    Triplanar triplanar;
    triplanar.normal = normalize(vertexNormal);
    triplanar.weights = getTriplanarWeights(triplanar.normal);

    for (int plane = 0; plane < 3; plane++) {
        triplanar.coordinates[plane] = getTriplanarCoordinates(plane, vertexPosition, triplanar.normal, worldSize);
        triplanar.dx[plane] = dFdx(triplanar.coordinates[plane]);
        triplanar.dy[plane] = dFdy(triplanar.coordinates[plane]);
    }

    return triplanar;
}

vec2 getTriplanarTextureCoordinates(Triplanar triplanar, int plane, int layer) {
    return triplanar.coordinates[plane] * materials[layer].tiling + materials[layer].offset;
}

vec4 sampleTriplanar(sampler2DArray textures, int layer, Triplanar triplanar) {
    vec4 result = vec4(0.0);
    for (int plane = 0; plane < 3; plane++)
        if (triplanar.weights[plane] > 0.001)
            result += sampleWithoutRepetition(textures, getTriplanarTextureCoordinates(triplanar, plane, layer), float(layer),
                    triplanar.dx[plane] * materials[layer].tiling, triplanar.dy[plane] * materials[layer].tiling) * triplanar.weights[plane];

    return result;
}

/*
 * The same texture at a larger scale and turned, sampled from three sides: laid under the near one it hides the rows of the
 * repeat, because the two have different periods and directions (the colors of the distant tiling).
 */
const float FAR_SCALE = 0.21;
const float FAR_ANGLE = 0.65;

vec4 sampleTriplanarFar(sampler2DArray textures, int layer, Triplanar triplanar) {
    mat2 turn = mat2(cos(FAR_ANGLE), sin(FAR_ANGLE), -sin(FAR_ANGLE), cos(FAR_ANGLE));

    vec4 result = vec4(0.0);
    for (int plane = 0; plane < 3; plane++)
        if (triplanar.weights[plane] > 0.001)
            result += textureGrad(textures, vec3(turn * getTriplanarTextureCoordinates(triplanar, plane, layer) * FAR_SCALE, float(layer)),
                    turn * triplanar.dx[plane] * materials[layer].tiling * FAR_SCALE, turn * triplanar.dy[plane] * materials[layer].tiling * FAR_SCALE) * triplanar.weights[plane];

    return result;
}

/* The color of a material of the steep ground: the near scale and the far one, mixed by patches (by half, a bit more or less) */
vec3 sampleTriplanarColor(int layer, Triplanar triplanar) {
    vec3 near = sampleTriplanar(textureDiffuse, layer, triplanar).rgb;
    vec3 far = sampleTriplanarFar(textureDiffuse, layer, triplanar).rgb;

    return mix(near, far, mix(0.3, 0.7, getMacroVariation01()));
}

/* Every plane has its own normal map: the normals are brought into the world before they are mixed */
vec3 sampleTriplanarNormal(int layer, Triplanar triplanar) {
    vec3 result = vec3(0.0);
    for (int plane = 0; plane < 3; plane++)
        if (triplanar.weights[plane] > 0.001) {
            vec3 mapped = sampleWithoutRepetition(textureNormal, getTriplanarTextureCoordinates(triplanar, plane, layer), float(layer),
                    triplanar.dx[plane] * materials[layer].tiling, triplanar.dy[plane] * materials[layer].tiling).rgb * 2.0 - 1.0;
            result += getTriplanarNormal(plane, triplanar.normal, mapped) * triplanar.weights[plane];
        }

    return normalize(result);
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
    vec3 radiance = light.color * light.intensity * getLightFactor(light, vertexPosition) * getPointShadow(light, vertexPosition, normalize(vertexNormal));

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
    int current = heightMaterialCount - 1;
    for (int i = 0; i < heightMaterialCount; i++)
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

    if (current < heightMaterialCount - 1 && y > elevations[current] - halfWidth) {
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
    vec3 normal = applyDetail(getNormal(layer0, layer1, blend), blend < 0.5 ? layer0 : layer1);
    float roughness = getRoughness(layer0, layer1, blend);
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);

    // The materials of the steepness lie over the ones of the heights, sampled from three sides so they don't stretch
    float angle = getSlopeAngle(normalize(vertexNormal));
    Triplanar triplanar = getTriplanar();
    for (int i = 0; i < slopeCount; i++) {
        float weight = getSlopeWeight(angle, slopeMinimums[i], slopeMaximums[i], slopeBlends[i]);
        if (weight <= 0.0)
            continue;

        int layer = heightMaterialCount + i;
        baseColor = mix(baseColor, sampleTriplanarColor(layer, triplanar) * materials[layer].color.rgb * getBrightnessVariation() * getMacroVariation(), weight);

        if (isSet(useTextureNormal))
            normal = normalize(mix(normal, sampleTriplanarNormal(layer, triplanar), weight));

        roughness = mix(roughness, isSet(useTextureRoughness) ? sampleTriplanar(textureRoughness, layer, triplanar).r : materials[layer].roughness, weight);
    }

    outputColor = vec4(applyFog(computeLighting(baseColor, normal, cameraDirection, roughness)), 1.0);
}
