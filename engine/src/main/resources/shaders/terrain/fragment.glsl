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

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
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
in vec3 vertexTangent;
in vec3 vertexBitangent;
in vec3 vertexPosition;
in float vertexHeight;
in vec2 vertexTextureCoordinates;
in float vertexTimeElapsed;

/* Textures */
uniform samplerCube textureSkybox;
uniform sampler2DArray textureDiffuse;
uniform sampler2DArray textureNormal;
uniform sampler2DArray textureRoughness;
uniform sampler2DArray textureMetalness;
uniform sampler2DArray textureAmbientOcclusion;

/* Feature flags */
uniform int useDirectionalLight;
uniform int useTextureSkybox;
uniform int useTextureNormal;
uniform int useTextureRoughness;
uniform int useFog;

/* Fog */
uniform Fog fog;

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
 * Materials
 ***/

vec2 getTextureCoordinates(int layer) {
    return vertexTextureCoordinates * materials[layer].tiling + materials[layer].offset;
}

vec4 sampleLayers(sampler2DArray textures, int layer0, int layer1, float blend) {
    vec4 value0 = texture(textures, vec3(getTextureCoordinates(layer0), float(layer0)));
    vec4 value1 = texture(textures, vec3(getTextureCoordinates(layer1), float(layer1)));
    return mix(value0, value1, blend);
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

float getAttenuation(Attenuation attenuation, float distance) {
    // An all-zero attenuation would divide by zero
    return 1.0 / max(attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance, 0.0001);
}

/* Diffuse light takes the surface color, rough surfaces have weaker and wider highlights */
vec3 shade(vec3 baseColor, vec3 normal, vec3 lightDirection, vec3 cameraDirection, float roughness) {
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float shininess = mix(64.0, 4.0, roughness);
    float specular = diffuse > 0.0 ? pow(max(dot(reflect(-lightDirection, normal), cameraDirection), 0.0), shininess) * (1.0 - roughness) * 0.5 : 0.0;

    return diffuse * baseColor + vec3(specular);
}

vec3 calculateLight(Light light, vec3 baseColor, vec3 normal, vec3 cameraDirection, float roughness) {
    if (light.enabled == 0)
        return vec3(0.0);

    vec3 toLight = light.position - vertexPosition;
    float attenuation = getAttenuation(light.attenuation, length(toLight));

    return shade(baseColor, normal, normalize(toLight), cameraDirection, roughness) * light.color * light.intensity * attenuation;
}

vec3 calculateDirectionalLight(vec3 baseColor, vec3 normal, vec3 cameraDirection, float roughness) {
    if (!isSet(useDirectionalLight))
        return vec3(0.0);

    return shade(baseColor, normal, normalize(-directionalLight.direction), cameraDirection, roughness)
            * directionalLight.color * directionalLight.intensity;
}

vec3 computeLighting(vec3 baseColor, vec3 normal, vec3 cameraDirection, float roughness) {
    vec3 result = ambientLight * baseColor + calculateDirectionalLight(baseColor, normal, cameraDirection, roughness);

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], baseColor, normal, cameraDirection, roughness);

    return result;
}

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
    vec3 baseColor = sampleLayers(textureDiffuse, layer0, layer1, blend).rgb * materialColor.rgb;
    vec3 normal = getNormal(layer0, layer1, blend);
    float roughness = getRoughness(layer0, layer1, blend);
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);

    outputColor = vec4(applyFog(computeLighting(baseColor, normal, cameraDirection, roughness)), 1.0);
}
