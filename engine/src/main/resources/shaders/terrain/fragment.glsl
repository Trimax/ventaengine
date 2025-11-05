#version 330 core

/***
 * Definitions
 ***/

const int MAX_LIGHTS = 64;
const int MAX_MATERIALS = 8;

/***
 * Structures
 ***/

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct Light {
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation attenuation;
    int castShadows;
    int enabled;
};

struct DirectionalLight {
    vec3 color;
    vec3 direction;
    float intensity;
};

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
in vec3 vertexPosition;
in vec2 vertexTextureCoordinates;
in float vertexTimeElapsed;

/* Textures */
uniform samplerCube textureSkybox;
uniform sampler2D textureDiffuse[MAX_MATERIALS];
uniform sampler2D textureHeight[MAX_MATERIALS];
uniform sampler2D textureNormal[MAX_MATERIALS];
uniform sampler2D textureRoughness[MAX_MATERIALS];
uniform sampler2D textureMetalness[MAX_MATERIALS];
uniform sampler2D textureAmbientOcclusion[MAX_MATERIALS];
uniform sampler2D textureDebug;

/* Feature flags */
uniform int useDirectionalLight;
uniform int useTextureSkybox;

/* Materials */
uniform Material materials[MAX_MATERIALS];
uniform float elevations[MAX_MATERIALS];
uniform int materialCount;

/* Lighting */
uniform DirectionalLight directionalLight;
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

/* Camera */
uniform vec3 cameraPosition;

/* Time */
uniform float timeElapsed;

/* Output color */
out vec4 outputColor;

/***
 * Common functions
 ***/

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
}

/***
 * Lighting functions
 ***/

float getAttenuation(Attenuation attenuation, float distance) {
    return 1.0 / (attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance);
}

/* Computes light */
vec3 calculateLight(Light light, vec3 normal, vec3 cameraDirection) {
    if (light.enabled == 0)
        vec3(0.0);

    vec3 lightDirection = normalize(light.position - vertexPosition);
    float attenuation = getAttenuation(light.attenuation, length(light.position - vertexPosition));

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);
    float spec = pow(max(dot(R, cameraDirection), 0.0), 32.0);

    return (diff + spec * 0.3) * light.color * light.intensity * attenuation;
}

/* Computes directional light */
vec3 computeDirectionalLight(vec3 normal, vec3 cameraDirection) {
    if (!isSet(useDirectionalLight))
        return vec3(0.0);

    vec3 lightDirection = normalize(-directionalLight.direction);

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);
    float spec = pow(max(dot(R, cameraDirection), 0.0), 32.0);

    return (diff + spec * 0.3) * directionalLight.color * directionalLight.intensity;
}

vec3 computeLighting(vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    vec3 result = ambientLight * baseColor;

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], normal, cameraDirection);

    return result;
}

/* Reflections */
void main() {
    outputColor = texture(textureDiffuse[0], vertexTextureCoordinates);
}
