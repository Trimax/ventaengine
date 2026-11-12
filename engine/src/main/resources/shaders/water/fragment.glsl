#version 330 core

/***
 * Definitions
 ***/

/* Light specific */
const int MAX_LIGHTS = 64;
const int MAX_NOISES = 16;

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

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
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
in float vertexHeight;
in vec2 vertexTextureCoordinates;
in float vertexTimeElapsed;

/* Textures */
uniform samplerCube textureSkybox;

/* Feature flags */
uniform int useDirectionalLight;
uniform int useTextureSkybox;
uniform int useMaterial;
uniform int useFog;

/* Lighting */
uniform DirectionalLight directionalLight;
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

/* Fog */
uniform Fog fog;

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

vec3 calculateColor() {
    // Height of the wave at this point relative to the calm surface; a calm surface is in the middle
    float amplitude = max(waveAmplitude, 0.0001);
    float heightFactor = clamp((vertexHeight + amplitude) / (2.0 * amplitude), 0.0, 1.0);
    vec3 waterColor = mix(material.colorDepth, material.colorSurface, heightFactor);

    return mix(waterColor, material.colorPeak, smoothstep(foam.threshold, 1.0, heightFactor) * foam.intensity);
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

/*
 * Ripples: every noise layer is a height field over the surface, its world space gradient tilts the normal.
 * The gradient doesn't depend on the screen resolution or the distance, unlike screen space derivatives.
 */
vec3 calculateNormal() {
    const float STEP = 0.05;
    vec3 normal = normalize(vertexNormal);

    for (int i = 0; i < noiseCount; i++) {
        vec2 uv = vertexPosition.xz * noises[i].scale + timeElapsed * noises[i].speed + noises[i].offset;

        float level = noise(uv);
        vec2 gradient = vec2(noise(uv + vec2(STEP, 0.0)) - level, noise(uv + vec2(0.0, STEP)) - level) / STEP * noises[i].scale;

        // Far away a pixel covers several ripples: they fade out instead of flickering (the surface gets smooth like a mirror)
        float detail = 1.0 - smoothstep(0.25, 1.0, length(fwidth(uv)));

        normal.xz -= gradient * noises[i].strength * detail;
    }

    // Far away the waves are smaller than a pixel too: the surface is seen as a smooth mirror reflecting the horizon
    float footprint = length(fwidth(vertexPosition.xz));
    return normalize(mix(normalize(normal), vec3(0.0, 1.0, 0.0), smoothstep(0.05, 0.5, footprint)));
}

/***
 * Lighting functions
 ***/

float getAttenuation(Attenuation attenuation, float distance) {
    // An all-zero attenuation would divide by zero
    return 1.0 / max(attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance, 0.0001);
}

/* Computes light: diffuse light takes the water color, specular highlights keep the light color */
vec3 calculateLight(Light light, vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    if (light.enabled == 0)
        return vec3(0.0);

    vec3 lightDirection = normalize(light.position - vertexPosition);
    float attenuation = getAttenuation(light.attenuation, length(light.position - vertexPosition));

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);
    float spec = pow(max(dot(R, cameraDirection), 0.0), 32.0);

    return (diff * baseColor + spec * 0.3) * light.color * light.intensity * attenuation;
}

/* Computes directional light */
vec3 computeDirectionalLight(vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    if (!isSet(useDirectionalLight))
        return vec3(0.0);

    vec3 lightDirection = normalize(-directionalLight.direction);

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);

    // The sun is small and bright: a sharp glint on the ripples
    float spec = pow(max(dot(R, cameraDirection), 0.0), 256.0) * 2.0 + pow(max(dot(R, cameraDirection), 0.0), 24.0) * 0.1;

    return (diff * baseColor + spec) * directionalLight.color * directionalLight.intensity;
}

vec3 computeLighting(vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    vec3 result = ambientLight * baseColor + computeDirectionalLight(baseColor, normal, cameraDirection);

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], baseColor, normal, cameraDirection);

    return result;
}

const float WATER_REFLECTANCE = 0.02;

vec3 applyReflections(vec3 color, vec3 cameraDirection, vec3 normal) {
    // The sun is reflected by the specular light, the environment is the sky
    if (!isSet(useTextureSkybox))
        return color;

    vec3 direction = reflect(-cameraDirection, normal);
    direction.y = abs(direction.y);
    vec3 environmentColor = texture(textureSkybox, direction).rgb;

    // Schlick fresnel with the reflectance of water (2% looking straight down, all of it at grazing angles)
    float cosine = clamp(dot(normal, cameraDirection), 0.0, 1.0);
    float fresnel = WATER_REFLECTANCE + (1.0 - WATER_REFLECTANCE) * pow(1.0 - cosine, 5.0);

    return mix(color, environmentColor, fresnel * material.metalness);
}

/***
 * Fog
 ***/

/* Part of the way between two points that lies in the fog layer (below fog.height) */
float getFogDistance(vec3 from, vec3 to) {
    float distance = length(to - from);
    float lower = min(from.y, to.y);
    float upper = max(from.y, to.y);

    if (upper <= fog.height)
        return distance;

    if (lower >= fog.height)
        return 0.0;

    return distance * (fog.height - lower) / (upper - lower);
}

/* Amount of fog color: 0 is no fog, 1 is the fog color only */
float getFogFactor(float distance) {
    float range = max(fog.maximalDistance - fog.minimalDistance, 0.0001);
    return clamp((distance - fog.minimalDistance) / range, 0.0, 1.0);
}

vec3 applyFog(vec3 color) {
    if (!isSet(useFog))
        return color;

    return mix(color, fog.color, getFogFactor(getFogDistance(cameraPosition, vertexPosition)));
}

void main() {
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);
    vec3 normal = calculateNormal();

    vec3 colorWithLighting = computeLighting(calculateColor(), normal, cameraDirection);
    vec3 colorWithReflections = applyReflections(colorWithLighting, cameraDirection, normal);

    outputColor = vec4(applyFog(clamp(colorWithReflections, 0.0, 1.0)), material.opacity);
}
