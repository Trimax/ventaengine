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
uniform sampler2DArray textureDiffuse;
uniform sampler2DArray textureHeight;
uniform sampler2DArray textureNormal;
uniform sampler2DArray textureRoughness;
uniform sampler2DArray textureMetalness;
uniform sampler2DArray textureAmbientOcclusion;
uniform sampler2D textureDebug;

/* Feature flags */
uniform int useDirectionalLight;
uniform int useTextureSkybox;

/* Materials */
uniform Material materials[MAX_MATERIALS];
uniform int materialCount;

/* Elevations & height */
uniform float elevations[MAX_MATERIALS];
uniform float factor;

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

vec2 getMaterialLayersAndBlend(float y) {
    int lower = 0;
    int upper = 0;
    float blend = 0.0;

    for (int i = 0; i < materialCount; i++) {
        if (y < elevations[i]) {
            upper = i;
            lower = max(i - 1, 0);
            float minElev = (lower == 0) ? 0.0 : elevations[lower];
            float maxElev = elevations[upper];
            blend = (y - minElev) / (maxElev - minElev);
            break;
        }
    }

    if (y >= elevations[materialCount - 1]) {
        lower = materialCount - 1;
        upper = materialCount - 1;
        blend = 0.0;
    }

    return vec2(float(lower), blend);
}

void main() {
    float minHeight = -0.5 * factor;
    float maxHeight =  0.5 * factor;

    float yNormalized = clamp((vertexPosition.y - minHeight) / (maxHeight - minHeight), 0.0, 1.0);
    vec2 info = getMaterialLayersAndBlend(yNormalized);
    int layer0 = int(info.x);
    int layer1 = min(layer0 + 1, materialCount - 1);
    float blend = info.y;

    vec4 color0 = texture(textureDiffuse, vec3(vertexTextureCoordinates, float(layer0)));
    vec4 color1 = texture(textureDiffuse, vec3(vertexTextureCoordinates, float(layer1)));

    outputColor = mix(color0, color1, blend);
}
