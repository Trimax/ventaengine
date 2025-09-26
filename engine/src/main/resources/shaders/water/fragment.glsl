#version 330 core

/***
 * Definitions
 ***/

/* Light specific */
const int MAX_LIGHTS = 64;
const int MAX_NOISES = 16;
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
    vec3 colorSurface;
    vec3 colorDepth;
    vec3 colorPeak;
    float metalness;
    float opacity;
};

struct Noise {
    float scale;
    float speed;
    float strength;
    float offset;
};

struct Foam {
    float threshold;
    float intensity;
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

/* Feature flags */
uniform int useTextureSkybox;
uniform int useMaterial;

/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

/* Noise */
uniform Noise noises[MAX_NOISES];
uniform int noiseCount;

/* Material */
uniform Material material;
uniform Foam foam;

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

float getMaterialMetalness() {
    return isSet(useMaterial) ? material.metalness : 0.0;
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
vec3 calculateNormal() {
    vec3 normal = vertexNormal;

    for (int i = 0; i < noiseCount; i++) {
        vec2 uv = vertexPosition.xz * noises[i].scale + timeElapsed * noises[i].speed + noises[i].offset;

        float noiseLevel = noise(uv);
        vec3 gradNormal = normalize(vec3(-dFdx(noiseLevel), 1.0, -dFdy(noiseLevel)));

        normal += (gradNormal - vertexNormal) * noises[i].strength;
    }

    return normalize(normal);
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

vec3 applyReflections(vec3 color, vec3 cameraDirection, vec3 normal) {
    if (!isSet(useTextureSkybox))
        return color;

    vec3 skyboxColor = texture(textureSkybox, reflect(-cameraDirection, normal)).rgb;

    float cosTheta = dot(normal, cameraDirection);
    float factor = pow(1.0 - cosTheta, 3.0);

    return mix(color, skyboxColor, factor * getMaterialMetalness());
}

vec3 calculateColor() {
    float heightFactor = clamp((vertexPosition.y + waveAmplitude) / (2.0 * waveAmplitude), 0.0, 1.0);
    vec3 waterColor = mix(material.colorDepth, material.colorSurface, heightFactor);

    return mix(waterColor, material.colorPeak, smoothstep(foam.threshold, 1.0, heightFactor) * foam.intensity);
}

void main() {
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);
    vec3 normal = calculateNormal();

    float fresnel = pow(1.0 - max(dot(normal, cameraDirection), 0.0), 5.0);
    vec3 colorWithFresnel = mix(calculateColor(), 0.8 * vec3(1.0, 0.6, 0.4), fresnel); //TODO: Use mix of directional lights

    vec3 color = computeLighting(colorWithFresnel, normal, cameraDirection);

    vec3 colorWithReflections = applyReflections(colorWithFresnel, cameraDirection, normal);

    outputColor = vec4(colorWithReflections, 1.0);
}