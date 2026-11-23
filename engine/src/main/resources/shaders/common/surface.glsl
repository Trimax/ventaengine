/*
 * The fragment shader of the objects: the physically based shading of a surface by its material. It has two variants,
 * both are made of this file: default/fragment.glsl and default/fragment-cutout.glsl, which defines ALPHA_CUTOUT and
 * cuts the holes of the cutout materials.
 */

#include "common/environment.glsl"
#include "common/flags.glsl"
#include "common/fog.glsl"
#include "common/lights.glsl"
#include "common/pbr.glsl"
#include "common/shadow.glsl"

/***
 * Structures
 ***/

struct Material {
    vec4 color;
    vec2 tiling;
    vec2 offset;
    float metalness;
    float roughness;
    float alphaCutoff;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec4 vertexColor;
in vec3 vertexPosition;
in vec3 vertexCameraPosition;
in vec2 vertexTextureCoordinates;

in mat3 vertexTBN;

/* Textures */
uniform sampler2D textureDiffuse;
uniform sampler2D textureHeight;
uniform sampler2D textureNormal;
uniform sampler2D textureRoughness;
uniform sampler2D textureMetalness;
uniform sampler2D textureAmbientOcclusion;

/* Feature flags */
uniform int useTextureDiffuse;
uniform int useTextureHeight;
uniform int useTextureNormal;
uniform int useTextureRoughness;
uniform int useTextureMetalness;
uniform int useTextureAmbientOcclusion;
uniform int useLighting;
uniform int useMaterial;
#ifdef ALPHA_CUTOUT
uniform int useAlphaCutout;
#endif

/* Material */
uniform Material material;

/* Output color */
out vec4 outputColor;

/***
 * View direction
 ***/

/* Computed per fragment: interpolating a normalized per-vertex direction is wrong on large triangles */
vec3 getViewDirectionWorldSpace() {
    return normalize(vertexCameraPosition - vertexPosition);
}

vec3 getViewDirectionLocalSpace() {
    return normalize(transpose(vertexTBN) * getViewDirectionWorldSpace());
}

/* Gets base color from the texture */
vec4 getDiffuseColor(vec2 textureCoordinates) {
    return isSet(useTextureDiffuse) ? texture(textureDiffuse, textureCoordinates) : vec4(1.0);
}

/* Gets ambient occlusion */
float getAmbientOcclusion(vec2 textureCoordinates) {
    return isSet(useTextureAmbientOcclusion) ? texture(textureAmbientOcclusion, textureCoordinates).r : 1.0;
}

/* Gets height */
float getHeight(vec2 textureCoordinates) {
    return isSet(useTextureHeight) ? texture(textureHeight, textureCoordinates).r : 1.0;
}

/* Gets roughness */
float getRoughness(vec2 textureCoordinates) {
    if (isSet(useTextureRoughness))
        return texture(textureRoughness, textureCoordinates).r;

    if (isSet(useMaterial))
        return material.roughness;

    return 0.0;
}

/* Gets metalness */
float getMetalness(vec2 textureCoordinates) {
    if (isSet(useTextureMetalness))
        return texture(textureMetalness, textureCoordinates).r;

    if (isSet(useMaterial))
        return material.metalness;

    return 0.0;
}

/* Translates texture coordinates according to parallax effect */
vec2 parallaxMapping(vec2 textureCoordinates) {
    if (!isSet(useTextureHeight))
        return textureCoordinates;

    return textureCoordinates + getViewDirectionLocalSpace().xy * (getHeight(textureCoordinates) * 0.01);
}

/* The side of a face that is seen: both sides of a double sided face are lit by the normal that faces the viewer */
float getFaceSign() {
    return gl_FrontFacing ? 1.0 : -1.0;
}

/* Gets the normal of the face (not from the normal map) in the world space, turned to the seen side of the face */
vec3 getGeometricNormal() {
    return normalize(vertexTBN[2]) * getFaceSign();
}

/* Gets normal (either face or from normal map) */
vec3 getNormal(vec2 textureCoordinates) {
    if (!isSet(useTextureNormal))
        return getGeometricNormal();

    /* Normal mapping */
    return normalize(vertexTBN * (texture(textureNormal, textureCoordinates).rgb * 2.0 - 1.0)) * getFaceSign();
}

/* Gets preprocessed texture coordinates */
vec2 getTextureCoordinates() {
    vec2 textureCoordinates = isSet(useMaterial) ? (vertexTextureCoordinates * material.tiling + material.offset) : vertexTextureCoordinates;

    return isSet(useTextureHeight) ? parallaxMapping(textureCoordinates) : textureCoordinates;
}

/* Gets material color if set othewise white color */
vec4 getMaterialColor() {
    return isSet(useMaterial) ? material.color : vec4(1.0);
}

/***
 * Lighting
 ***/

/* Calculates effect of one light source */
vec3 calculateLight(vec3 albedo, float metalness, float roughness, Light light, vec3 normal, vec3 viewDirection) {
    if (!isSet(light.enabled))
        return vec3(0.0);

    vec3 toLight = light.position - vertexPosition;
    float distance = length(toLight);
    vec3 radiance = light.color * light.intensity * getAttenuation(light.attenuation, distance);

    return pbrLight(normal, viewDirection, toLight / max(distance, 0.0001), albedo, metalness, roughness, radiance);
}

/* Calculates effect of directional light if present */
vec3 calculateDirectionalLight(vec3 albedo, float metalness, float roughness, vec3 normal, vec3 viewDirection) {
    if (!isSet(useDirectionalLight))
        return vec3(0.0);

    vec3 radiance = directionalLight.color * directionalLight.intensity * getSunShadow(vertexPosition, getGeometricNormal());

    return pbrLight(normal, viewDirection, normalize(-directionalLight.direction), albedo, metalness, roughness, radiance);
}

/* The color of a surface: the light of the environment, the sun and the lights reflected by the material */
vec4 applyLighting(vec4 color, vec2 textureCoordinates) {
    float occlusion = getAmbientOcclusion(textureCoordinates);
    if (!isSet(useLighting))
        return vec4(color.rgb * occlusion, color.a);

    vec3 normal = getNormal(textureCoordinates);
    vec3 viewDirection = getViewDirectionWorldSpace();
    float metalness = getMetalness(textureCoordinates);
    float roughness = getRoughness(textureCoordinates);

    vec3 lighting = getAmbientLight(normal, viewDirection, color.rgb, metalness, roughness, occlusion)
            + calculateDirectionalLight(color.rgb, metalness, roughness, normal, viewDirection);

    for (int i = 0; i < lightCount; i++)
        lighting += calculateLight(color.rgb, metalness, roughness, lights[i], normal, viewDirection);

    // The result is HDR: the tone mapping of the post processing brings it to the screen
    return vec4(lighting, color.a);
}

/***
 * Fog
 ***/

vec4 applyFog(vec4 color) {
    if (!isSet(useFog))
        return color;

    return vec4(mix(color.rgb, fog.color.rgb, getFogAmount(vertexCameraPosition, vertexPosition)), color.a);
}

void main() {
    vec2 textureCoordinates = getTextureCoordinates();

    vec4 colorWithoutEffects = vertexColor * getDiffuseColor(textureCoordinates) * getMaterialColor();

#ifdef ALPHA_CUTOUT
    // A cutout surface is a hole or opaque: it has no partly transparent fragments
    if (isSet(useAlphaCutout)) {
        if (colorWithoutEffects.a < material.alphaCutoff)
            discard;

        colorWithoutEffects.a = 1.0;
    }
#endif

    vec4 colorWithLighting = applyLighting(colorWithoutEffects, textureCoordinates);
    vec4 colorWithFog = applyFog(colorWithLighting);

    outputColor = colorWithFog;
}
