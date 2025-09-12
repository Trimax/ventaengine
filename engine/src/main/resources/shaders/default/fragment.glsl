#version 330 core

/***
 * Definitions
 ***/

/* Common */
const float METALNESS_MINIMAL = 0.04;

/* Light specific */
const int MAX_LIGHTS = 64;
const int LIGHT_TYPE_POINT = 0;
const int LIGHT_TYPE_DIRECTIONAL = 1;
const int LIGHT_TYPE_SPOT = 2;

/***
 * Structures
 ***/

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct Light {
    int type;
    vec3 position;
    vec3 direction;
    vec3 color;
    float intensity;
    Attenuation attenuation;
    int castShadows;
    int enabled;
};

struct Material {
    vec4 color;
    vec2 tiling;
    vec2 offset;
    float metalness;
    float roughness;
};

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec4 vertexColor;
in vec3 vertexPosition;
in vec3 vertexCameraPosition;
in vec2 vertexTextureCoordinates;
in vec3 vertexViewDirectionLocalSpace;
in vec3 vertexViewDirectionWorldSpace;

in mat3 vertexTBN;

/* Textures */
uniform samplerCube textureSkybox;
uniform sampler2D textureDiffuse;
uniform sampler2D textureHeight;
uniform sampler2D textureNormal;
uniform sampler2D textureRoughness;
uniform sampler2D textureMetalness;
uniform sampler2D textureAmbientOcclusion;
uniform sampler2D textureDebug;

/* Feature flags */
uniform int useTextureSkybox;
uniform int useTextureDiffuse;
uniform int useTextureHeight;
uniform int useTextureNormal;
uniform int useTextureRoughness;
uniform int useTextureMetalness;
uniform int useTextureAmbientOcclusion;
uniform int useTextureDebug;
uniform int useLighting;
uniform int useMaterial;
uniform int useFog;

/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

/* Material */
uniform Material material;

/* Fog */
uniform Fog fog;

/* Output color */
out vec4 outputColor;

/***
 * Common functions
 ***/

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
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

    return textureCoordinates + vertexViewDirectionLocalSpace.xy * (getHeight(textureCoordinates) * 0.01);
}

/* Gets normal (either face or from normal map) */
vec3 getNormal(vec2 textureCoordinates) {
    if (!isSet(useTextureNormal))
        return vertexTBN[2];

    /* Normal mapping */
    return normalize(vertexTBN * (texture(textureNormal, textureCoordinates).rgb * 2.0 - 1.0));
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

float getAttenuation(Attenuation attenuation, float distance) {
    return 1.0 / (attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance);
}

/* Calculates diffuse light component */
vec3 calculateLightDiffuse(vec3 lightColor, vec3 normal, vec3 lightDirection) {
    return max(dot(normal, lightDirection), 0.0) * lightColor;
}

/* Calculates specular light component */
vec3 calculateLightSpecular(vec4 color, vec3 lightColor, vec3 normal, vec3 lightDirection, vec3 viewDirection, vec2 textureCoordinates) {
    vec3 F0 = mix(vec3(METALNESS_MINIMAL), color.rgb, getMetalness(textureCoordinates));
    float shininess = mix(1.0, 256.0, 1.0 - getRoughness(textureCoordinates));
    float factorSpecular = pow(max(dot(vertexViewDirectionWorldSpace, reflect(-lightDirection, normal)), 0.0), shininess);

    return F0 * factorSpecular * lightColor;
}

/* Calculates effect of one light source */
vec3 calculateLight(vec4 color, Light light, vec3 normal, vec3 fragmentPosition, vec2 textureCoordinates) {
    if (!isSet(light.enabled))
        return vec3(0.0);

    vec3 lightDirection = vec3(0.0);
    float attenuation = 1.0;

    switch (light.type) {
        case LIGHT_TYPE_POINT:
        case LIGHT_TYPE_SPOT:
            lightDirection = normalize(light.position - fragmentPosition);
            attenuation = getAttenuation(light.attenuation, length(light.position - fragmentPosition));
            break;

        case LIGHT_TYPE_DIRECTIONAL:
            lightDirection = normalize(-light.direction);
            break;

        default:
            return vec3(0.0);
    }

    vec3 colorDiffuse = calculateLightDiffuse(light.color, normal, lightDirection);
    vec3 colorSpecular = calculateLightSpecular(color, light.color, normal, lightDirection, vertexViewDirectionWorldSpace, textureCoordinates);

    return (colorDiffuse + colorSpecular) * light.intensity * attenuation;
}

vec3 calculateLighting(vec4 color, vec2 textureCoordinates) {
    if (!isSet(useLighting))
        return vec3(1.0);

    vec3 normal = getNormal(textureCoordinates);

    vec3 lighting = ambientLight.xyz;
    for (int i = 0; i < lightCount; i++)
        lighting += calculateLight(color, lights[i], normal, vertexPosition, textureCoordinates);

    return lighting;
}

vec4 applyLighting(vec4 color, vec2 textureCoordinates) {
    vec3 lighting = calculateLighting(color, textureCoordinates) * getAmbientOcclusion(textureCoordinates);

    return vec4(clamp(color.rgb * lighting, 0.0, 1.0), color.a);
}

/***
 * Reflections
 ***/

vec4 applyReflections(vec4 color, vec2 textureCoordinates) {
    if (!isSet(useTextureSkybox))
        return color;

    vec3 N = normalize(getNormal(textureCoordinates));
    vec3 V = normalize(vertexViewDirectionWorldSpace);
    vec4 skyboxColor = vec4(texture(textureSkybox, reflect(-V, N)).rgb, 1.0);

    return mix(color, skyboxColor, getMetalness(textureCoordinates));
}

/***
 * Fog
 ***/

float computeFogFactor(float distance) {
    return clamp((distance - fog.minimalDistance) / (fog.maximalDistance - fog.minimalDistance), 0.0, 1.0);
}

vec4 applyFog(vec4 color) {
    if (!isSet(useFog))
        return color;

    return mix(color, vec4(fog.color, 1.0), computeFogFactor(length(vertexCameraPosition - vertexPosition)));
}

void main() {
    vec2 textureCoordinates = getTextureCoordinates();

    vec4 colorWithoutEffects = vertexColor * getDiffuseColor(textureCoordinates) * getMaterialColor();
    vec4 colorWithLighting = applyLighting(colorWithoutEffects, textureCoordinates);
    vec4 colorWithReflections = applyReflections(colorWithLighting, textureCoordinates);
    vec4 colorWithFog = applyFog(colorWithReflections);

    outputColor = colorWithFog;
}