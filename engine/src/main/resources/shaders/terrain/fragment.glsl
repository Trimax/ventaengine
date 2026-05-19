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

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in mat3 vertexTBN;
in vec3 vertexPosition;
in vec2 vertexTextureCoordinates;
in vec3 vertexViewDirectionLocalSpace;
in vec3 vertexViewDirectionWorldSpace;
in float vertexHeightNormalized;

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
uniform int useTextureHeight;
uniform int useTextureDiffuse;
uniform int useTextureNormal;
uniform int useTextureRoughness;
uniform int useTextureMetalness;
uniform int useTextureAmbientOcclusion;
uniform int useFog;

/* Materials */
uniform Material materials[MAX_MATERIALS];
uniform int materialCount;

/* Elevations & height */
uniform float elevations[MAX_MATERIALS];
uniform float blendWidth;
uniform float factor;

/* Lighting */
uniform DirectionalLight directionalLight;
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

/* Fog */
uniform Fog fog;

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
 * Texture sampling functions
 ***/

/* Computes per-material texture coordinates with tiling and offset */
vec2 getMaterialTextureCoordinates(int layerIndex) {
    return vertexTextureCoordinates * materials[layerIndex].tiling + materials[layerIndex].offset;
}

/* Applies parallax mapping offset to texture coordinates for a given layer */
vec2 applyParallaxMapping(vec2 uv, int layerIndex) {
    if (!isSet(useTextureHeight))
        return uv;

    float heightSample = texture(textureHeight, vec3(uv, float(layerIndex))).r;
    return uv + vertexViewDirectionLocalSpace.xy * heightSample * 0.01;
}

/* Samples diffuse texture array for a layer, falls back to material color if not bound */
vec4 sampleDiffuse(vec2 uv, int layerIndex) {
    if (!isSet(useTextureDiffuse))
        return vec4(1.0);
    return texture(textureDiffuse, vec3(uv, float(layerIndex)));
}

/* Samples normal texture array and transforms to world space via TBN */
vec3 sampleNormal(vec2 uv, int layerIndex) {
    if (!isSet(useTextureNormal))
        return normalize(vertexTBN[2]); // Use geometric normal (TBN column 2 = N)
    vec3 normalSample = texture(textureNormal, vec3(uv, float(layerIndex))).rgb;
    return normalize(vertexTBN * (normalSample * 2.0 - 1.0));
}

/* Samples roughness texture array for a layer */
float sampleRoughness(vec2 uv, int layerIndex) {
    if (!isSet(useTextureRoughness))
        return materials[layerIndex].roughness;
    return texture(textureRoughness, vec3(uv, float(layerIndex))).r;
}

/* Samples metalness texture array for a layer */
float sampleMetalness(vec2 uv, int layerIndex) {
    if (!isSet(useTextureMetalness))
        return materials[layerIndex].metalness;
    return texture(textureMetalness, vec3(uv, float(layerIndex))).r;
}

/* Samples ambient occlusion texture array for a layer */
float sampleAmbientOcclusion(vec2 uv, int layerIndex) {
    if (!isSet(useTextureAmbientOcclusion))
        return 1.0;
    return texture(textureAmbientOcclusion, vec3(uv, float(layerIndex))).r;
}

/***
 * Material blending
 ***/

vec2 getMaterialLayersAndBlend(float yNormalized) {
    // Handle single layer
    if (materialCount < 2) {
        return vec2(0.0, 0.0);
    }

    // Find which layer this height belongs to.
    // elevation[i] means "from this height, use material i".
    // So material i is active from elevations[i] to elevations[i+1].
    int layer = 0;
    for (int i = 1; i < MAX_MATERIALS; i++) {
        if (i >= materialCount) break;
        if (yNormalized >= elevations[i]) {
            layer = i;
        } else {
            break;
        }
    }

    // Compute blend with the next layer at the boundary.
    // Blending occurs in a narrow band (blendWidth) around each elevation threshold.
    int nextLayer = min(layer + 1, materialCount - 1);
    if (layer == nextLayer) {
        // Top-most layer, no blending
        return vec2(float(layer), 0.0);
    }

    float threshold = elevations[nextLayer];
    float halfBlend = blendWidth * 0.5;
    float blendStart = threshold - halfBlend;
    float blendEnd = threshold + halfBlend;

    if (yNormalized <= blendStart) {
        return vec2(float(layer), 0.0);
    }
    if (yNormalized >= blendEnd) {
        return vec2(float(nextLayer), 0.0);
    }

    // Within blend region: smoothstep between current and next layer
    float blend = smoothstep(blendStart, blendEnd, yNormalized);
    return vec2(float(layer), blend);
}

/***
 * Lighting functions
 ***/

float getAttenuation(Attenuation attenuation, float distance) {
    return 1.0 / (attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance);
}

/* Computes PBR lighting with ambient, directional, and point lights */
vec3 computePBRLighting(vec4 color, vec3 N, float roughness, float metalness, float ao) {
    vec3 V = normalize(vertexViewDirectionWorldSpace);

    /* Derive PBR parameters */
    vec3 F0 = mix(vec3(0.04), color.rgb, metalness);
    float shininess = mix(1.0, 256.0, 1.0 - roughness);

    /* Ambient contribution */
    vec3 result = ambientLight * color.rgb;

    /* Directional light contribution */
    if (isSet(useDirectionalLight)) {
        vec3 lightDir = normalize(-directionalLight.direction);

        float diff = max(dot(N, lightDir), 0.0);
        vec3 diffuse = diff * directionalLight.color * directionalLight.intensity * color.rgb;

        vec3 R = reflect(-lightDir, N);
        float spec = pow(max(dot(V, R), 0.0), shininess);
        vec3 specular = F0 * spec * directionalLight.color * directionalLight.intensity;

        result += diffuse + specular;
    }

    /* Point lights contribution */
    for (int i = 0; i < lightCount; i++) {
        if (lights[i].enabled == 0)
            continue;

        vec3 lightDir = normalize(lights[i].position - vertexPosition);
        float dist = length(lights[i].position - vertexPosition);
        float attenuation = getAttenuation(lights[i].attenuation, dist);

        float diff = max(dot(N, lightDir), 0.0);
        vec3 diffuse = diff * lights[i].color * lights[i].intensity * color.rgb;

        vec3 R = reflect(-lightDir, N);
        float spec = pow(max(dot(V, R), 0.0), shininess);
        vec3 specular = F0 * spec * lights[i].color * lights[i].intensity;

        result += (diffuse + specular) * attenuation;
    }

    /* Apply ambient occlusion */
    result *= ao;

    /* Clamp final lit color */
    return clamp(result, 0.0, 1.0);
}

/***
 * Reflections
 ***/

/* Applies skybox reflections mixed with lit color by metalness */
vec3 applyReflections(vec3 color, vec3 N, float metalness) {
    if (!isSet(useTextureSkybox))
        return color;

    vec3 V = normalize(vertexViewDirectionWorldSpace);
    vec3 reflectDir = reflect(-V, N);
    vec3 skyboxColor = texture(textureSkybox, reflectDir).rgb;

    return mix(color, skyboxColor, metalness);
}

/***
 * Fog
 ***/

/* Computes distance-based fog factor */
float computeFogFactor(float distance) {
    return clamp((distance - fog.minimalDistance) / (fog.maximalDistance - fog.minimalDistance), 0.0, 1.0);
}

/* Applies fog to a color based on camera-to-fragment distance */
vec3 applyFog(vec3 color) {
    if (!isSet(useFog))
        return color;

    float dist = length(cameraPosition - vertexPosition);
    return mix(color, fog.color, computeFogFactor(dist));
}

void main() {
    float yNormalized = vertexHeightNormalized;
    vec2 info = getMaterialLayersAndBlend(yNormalized);
    int layer0 = int(info.x);
    int layer1 = min(layer0 + 1, materialCount - 1);
    float blend = info.y;

    /* Compute texture coordinates with tiling/offset for each layer */
    vec2 uv0 = getMaterialTextureCoordinates(layer0);
    vec2 uv1 = getMaterialTextureCoordinates(layer1);

    /* Apply parallax mapping per layer */
    uv0 = applyParallaxMapping(uv0, layer0);
    uv1 = applyParallaxMapping(uv1, layer1);

    /* Sample all PBR channels for layer 0 */
    vec4 diffuse0 = sampleDiffuse(uv0, layer0) * materials[layer0].color;
    vec3 normal0 = sampleNormal(uv0, layer0);
    float roughness0 = sampleRoughness(uv0, layer0);
    float metalness0 = sampleMetalness(uv0, layer0);
    float ao0 = sampleAmbientOcclusion(uv0, layer0);

    /* Sample all PBR channels for layer 1 */
    vec4 diffuse1 = sampleDiffuse(uv1, layer1) * materials[layer1].color;
    vec3 normal1 = sampleNormal(uv1, layer1);
    float roughness1 = sampleRoughness(uv1, layer1);
    float metalness1 = sampleMetalness(uv1, layer1);
    float ao1 = sampleAmbientOcclusion(uv1, layer1);

    /* Blend all PBR channels between the two layers */
    vec4 blendedDiffuse = mix(diffuse0, diffuse1, blend);
    vec3 blendedNormal = normalize(mix(normal0, normal1, blend));
    float blendedRoughness = mix(roughness0, roughness1, blend);
    float blendedMetalness = mix(metalness0, metalness1, blend);
    float blendedAO = mix(ao0, ao1, blend);

    /* Apply PBR lighting or pass through unlit */
    vec3 litColor = computePBRLighting(blendedDiffuse, blendedNormal, blendedRoughness, blendedMetalness, blendedAO);

    /* Apply skybox reflections (if skybox bound) */
    litColor = applyReflections(litColor, blendedNormal, blendedMetalness);

    /* Apply fog (if enabled) */
    litColor = applyFog(litColor);

    outputColor = vec4(litColor, blendedDiffuse.a);
}
