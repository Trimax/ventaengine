#version 330 core

/***
 * Definitions
 ***/

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
    vec3 color;
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
uniform sampler2D textureDiffuse;
uniform sampler2D textureNormal;

/* Feature flags */
uniform int useTextureSkybox;
uniform int useTextureDiffuse;
uniform int useTextureNormal;
uniform int useMaterial;

/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

/* Material */
uniform Material material;

/* Camera */
uniform vec3 cameraPosition;

/* Waves */
uniform float waveAmplitude;

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

/* Gets preprocessed texture coordinates */
vec2 getTextureCoordinates() {
    return isSet(useMaterial) ? (vertexTextureCoordinates * material.tiling + material.offset) : vertexTextureCoordinates;
}

vec3 getMaterialColor() {
    return isSet(useMaterial) ? material.color.rgb : vec3(1.0);
}

float getMaterialMetalness() {
    return isSet(useMaterial) ? material.metalness : 0.0;
}

/* Gets diffuse texture color */
vec3 getColor(vec2 textureCoordinates) {
    if (!isSet(useTextureDiffuse))
        return getMaterialColor();

    return mix(getMaterialColor(), texture(textureDiffuse, textureCoordinates).rgb, 0.5);
}

/* Perlin noise */
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

/* Computes normal  */
vec3 getNormal(vec2 textureCoordinates) {
    vec2 noiseUV = vertexPosition.xz * 0.2 + timeElapsed * 0.6;

    return normalize(vec3(vertexNormal.x, vertexNormal.y + (noise(noiseUV) - 0.5) * 0.1, vertexNormal.z));
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

    vec3 lightDirection = vec3(0.0);
    float attenuation = 1.0;

    switch (light.type) {
        case LIGHT_TYPE_POINT:
        case LIGHT_TYPE_SPOT:
            lightDirection = normalize(light.position - vertexPosition);
            attenuation = getAttenuation(light.attenuation, length(light.position - vertexPosition));
            break;

        case LIGHT_TYPE_DIRECTIONAL:
            lightDirection = normalize(-light.direction);
            break;

        default:
            return vec3(0.0);
    }

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);
    float spec = pow(max(dot(R, cameraDirection), 0.0), 32.0);

    return (diff + spec * 0.3) * light.color * light.intensity * attenuation;
}

vec3 computeLighting(vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    vec3 result = ambientLight * baseColor;

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], normal, cameraDirection);

    return result;
}

vec3 applyReflections(vec3 color, vec3 cameraDirection, vec2 textureCoordinates) {
    if (!isSet(useTextureSkybox))
        return color;

    vec3 normal = normalize(getNormal(textureCoordinates));
    vec3 skyboxColor = texture(textureSkybox, reflect(-cameraDirection, normal)).rgb;

    float cosTheta = dot(normal, cameraDirection);
    float factor = pow(1.0 - cosTheta, 3.0);

    return mix(color, skyboxColor, factor * getMaterialMetalness());
}

void main() {
    vec2 textureCoordinates = getTextureCoordinates();

    vec3 surfaceColor = vec3(0.12f, 0.52f, 0.65f);
    vec3 depthColor = vec3(0.01f, 0.13f, 0.28f);
    vec3 peakColor = vec3(0.95f, 0.97f, 1.f);

    float heightFactor = clamp((vertexPosition.y + waveAmplitude) / (2.0 * waveAmplitude), 0.0, 1.0);
    vec3 waterColor = mix(depthColor, surfaceColor, heightFactor);

    float foamThreshold = 0.85;
    float foamAmount = smoothstep(foamThreshold, 1.0, heightFactor);

    vec3 finalColor = mix(waterColor, peakColor, foamAmount);

    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);
    vec3 baseColor = getColor(textureCoordinates);

    float fresnel = pow(1.0 - max(dot(getNormal(textureCoordinates), cameraDirection), 0.0), 5.0);
    vec3 colorWithFresnel = mix(finalColor, 0.8 * vec3(1.0, 0.6, 0.4), fresnel); //TODO: Use mix of directional lights

    vec3 color = computeLighting(colorWithFresnel, getNormal(textureCoordinates), cameraDirection);

    vec3 colorWithReflections = applyReflections(colorWithFresnel, cameraDirection, textureCoordinates);

    outputColor = vec4(colorWithReflections, 1.0);
}